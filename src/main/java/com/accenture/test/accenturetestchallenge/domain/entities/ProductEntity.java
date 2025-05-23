package com.accenture.test.accenturetestchallenge.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class ProductEntity {

  @Id private String id;
  private String name;
  private Integer stock;
  private String branchId;
  private String franchiseId;
}
