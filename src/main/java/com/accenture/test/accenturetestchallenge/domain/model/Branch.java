package com.accenture.test.accenturetestchallenge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Branch {

  private String franchiseId;
  private String id;
  private String name;
}
