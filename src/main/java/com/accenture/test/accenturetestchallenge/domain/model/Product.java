package com.accenture.test.accenturetestchallenge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

  private String franchiseId;
  private String branchId;
  private String id;
  private String name;
  private Integer stock;

  public Product(String name, Integer stock) {
    this.name = name;
    this.stock = stock;
  }
}
