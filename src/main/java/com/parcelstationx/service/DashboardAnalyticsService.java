package com.parcelstationx.service;

import com.parcelstationx.dao.ExceptionRecordDao;
import com.parcelstationx.dao.OperationLogDao;
import com.parcelstationx.model.ExceptionRecord;
import com.parcelstationx.model.OperationLog;
import com.parcelstationx.model.Parcel;
import com.parcelstationx.model.ParcelStatus;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Builds dashboard read models from DAO-backed business data; HTTP/UI layers never issue SQL. */
public final class DashboardAnalyticsService {
  private final WarehouseLayoutService warehouse;
  private final ExceptionRecordDao exceptions;
  private final OperationLogDao operations;

  public DashboardAnalyticsService(
      WarehouseLayoutService warehouse,
      ExceptionRecordDao exceptions,
      OperationLogDao operations) {
    this.warehouse = warehouse;
    this.exceptions = exceptions;
    this.operations = operations;
  }

  public Trends trends() {
    var parcels = warehouse.snapshot().parcels();
    List<DailyPoint> points = new ArrayList<>();
    for (int offset = 13; offset >= 0; offset--) {
      LocalDate day = LocalDate.now().minusDays(offset);
      long inbound = parcels.stream().filter(p -> p.arrivedAt().toLocalDate().equals(day)).count();
      long outbound = parcels.stream().filter(p -> p.pickedUpAt() != null && p.pickedUpAt().toLocalDate().equals(day)).count();
      long inventory = parcels.stream().filter(p -> !p.arrivedAt().toLocalDate().isAfter(day) && (p.pickedUpAt() == null || p.pickedUpAt().toLocalDate().isAfter(day))).count();
      points.add(new DailyPoint(day.toString(), inbound, outbound, inventory));
    }
    return new Trends(points);
  }

  public Distributions distributions() {
    var snapshot = warehouse.snapshot();
    Map<String, Long> courier = count(snapshot.parcels().stream().map(Parcel::courierCompany).toList());
    Map<String, Long> status = count(snapshot.parcels().stream().map(p -> p.status().name()).toList());
    Map<String, Long> exception = count(exceptions.findAll().stream().map(e -> e.exceptionType().name()).toList());
    Map<String, Double> shelf = new LinkedHashMap<>(), zoneTotals = new LinkedHashMap<>(), zoneUsed = new LinkedHashMap<>();
    snapshot.shelves().forEach(s -> { shelf.put(s.shelfCode(), percent(s.occupied(), s.capacity())); zoneTotals.merge(s.zone(), (double)s.capacity(), Double::sum); zoneUsed.merge(s.zone(), (double)s.occupied(), Double::sum); });
    Map<String, Double> zone = new LinkedHashMap<>(); zoneTotals.forEach((key,total) -> zone.put(key, percent(zoneUsed.getOrDefault(key,0d), total)));
    Map<String, Long> dwell = new LinkedHashMap<>(); dwell.put("0-1天",0L);dwell.put("1-3天",0L);dwell.put("3-7天",0L);dwell.put("7天以上",0L);
    LocalDateTime now=LocalDateTime.now();snapshot.parcels().stream().filter(p->p.status()!=ParcelStatus.PICKED_UP).forEach(p->{long days=Duration.between(p.arrivedAt(),now).toDays();String key=days<1?"0-1天":days<3?"1-3天":days<7?"3-7天":"7天以上";dwell.merge(key,1L,Long::sum);});
    return new Distributions(courier,status,exception,shelf,zone,dwell);
  }

  public Activity activity() {
    List<ExceptionRecord> recentExceptions=exceptions.findAll().stream().sorted((a,b)->b.createdAt().compareTo(a.createdAt())).limit(8).toList();
    List<OperationLog> recentOperations=operations.findAll().stream().sorted((a,b)->b.createdAt().compareTo(a.createdAt())).limit(10).toList();
    return new Activity(recentExceptions,recentOperations);
  }

  private static Map<String,Long> count(List<String> values){return values.stream().collect(Collectors.groupingBy(x->x,LinkedHashMap::new,Collectors.counting()));}
  private static double percent(double used,double total){return total==0?0:Math.round(used*1000d/total)/10d;}
  public record DailyPoint(String date,long inbound,long outbound,long inventory) {}
  public record Trends(List<DailyPoint> points) {}
  public record Distributions(Map<String,Long> courier,Map<String,Long> status,Map<String,Long> exceptionType,Map<String,Double> shelfUtilization,Map<String,Double> zoneUtilization,Map<String,Long> dwell) {}
  public record Activity(List<ExceptionRecord> recentExceptions,List<OperationLog> recentOperations) {}
}
