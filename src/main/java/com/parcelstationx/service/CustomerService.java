package com.parcelstationx.service;

import com.parcelstationx.dao.CustomerDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.Customer;
import java.time.LocalDateTime;
import java.util.List;

public final class CustomerService {
  private final CustomerDao customers;

  public CustomerService(CustomerDao customers) {
    this.customers = customers;
  }

  public Customer create(String name, String mobile, String building, String room, String remark) {
    if (name == null || name.isBlank()) throw new BusinessException("客户姓名不能为空。");
    if (mobile == null || !mobile.matches("1\\d{10}")) throw new BusinessException("手机号格式无效。");
    LocalDateTime now = LocalDateTime.now();
    return customers.save(
        new Customer(null, name.trim(), mobile, building, room, remark, now, now));
  }

  public List<Customer> searchByMobile(String mobile) {
    return customers.findByMobile(mobile == null ? "" : mobile);
  }

  public Customer update(
      long id, String name, String mobile, String building, String room, String remark) {
    Customer current = customers.findById(id).orElseThrow(() -> new BusinessException("客户不存在。"));
    if (name == null || name.isBlank() || mobile == null || !mobile.matches("1\\d{10}"))
      throw new BusinessException("客户信息无效。");
    return customers.save(
        new Customer(
            id,
            name.trim(),
            mobile,
            building,
            room,
            remark,
            current.createdAt(),
            LocalDateTime.now()));
  }
}
