package com.parcelstationx.dao;
import com.parcelstationx.model.Customer; import java.util.List;
public interface CustomerDao extends BaseDao<Customer, Long> { List<Customer> findByMobile(String mobile); }
