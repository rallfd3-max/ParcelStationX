package com.parcelstationx.warehouseagent;

import com.parcelstationx.exception.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WarehousePlanStore {
  private final ConcurrentHashMap<UUID, WarehousePlan> plans = new ConcurrentHashMap<>();
  private final Clock clock;
  private final Duration ttl;

  public WarehousePlanStore() {
    this(Clock.systemUTC(), Duration.ofMinutes(5));
  }

  WarehousePlanStore(Clock clock, Duration ttl) {
    this.clock = clock;
    this.ttl = ttl;
  }

  public WarehousePlan create(
      long userId,
      WarehouseAgentAction action,
      com.parcelstationx.service.ShelfCreationResult preview) {
    Instant now = clock.instant();
    WarehousePlan plan =
        new WarehousePlan(
            UUID.randomUUID(),
            userId,
            now,
            now.plus(ttl),
            WarehousePlanStatus.PENDING,
            action,
            preview);
    plans.put(plan.id(), plan);
    return plan;
  }

  /** Reserves a pending plan while its Java service transaction is executing. */
  public synchronized WarehousePlan begin(UUID id, long userId) {
    WarehousePlan plan = require(id, userId);
    if (!clock.instant().isBefore(plan.expiresAt())) {
      plans.put(id, with(plan, WarehousePlanStatus.EXPIRED));
      throw new BusinessException("计划已过期。");
    }
    if (plan.status() != WarehousePlanStatus.PENDING) throw new BusinessException("计划不可重复确认。");
    WarehousePlan executing = with(plan, WarehousePlanStatus.EXECUTING);
    plans.put(id, executing);
    return executing;
  }

  public synchronized void complete(UUID id, long userId) {
    WarehousePlan plan = require(id, userId);
    if (plan.status() != WarehousePlanStatus.EXECUTING)
      throw new BusinessException("计划未处于执行状态。");
    plans.put(id, with(plan, WarehousePlanStatus.CONSUMED));
  }

  /** A failed transaction leaves the user with the reviewed plan rather than a false success. */
  public synchronized void releaseAfterFailure(UUID id, long userId) {
    WarehousePlan plan = require(id, userId);
    if (plan.status() == WarehousePlanStatus.EXECUTING)
      plans.put(id, with(plan, WarehousePlanStatus.PENDING));
  }

  public synchronized void cancel(UUID id, long userId) {
    WarehousePlan plan = require(id, userId);
    if (plan.status() == WarehousePlanStatus.PENDING)
      plans.put(id, with(plan, WarehousePlanStatus.CANCELLED));
  }

  private WarehousePlan require(UUID id, long userId) {
    WarehousePlan plan = plans.get(id);
    if (plan == null || plan.userId() != userId) throw new BusinessException("计划不存在或不属于当前用户。");
    return plan;
  }

  private WarehousePlan with(WarehousePlan p, WarehousePlanStatus s) {
    return new WarehousePlan(
        p.id(), p.userId(), p.createdAt(), p.expiresAt(), s, p.action(), p.preview());
  }
}
