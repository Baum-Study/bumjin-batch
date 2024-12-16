package com.example.batch.common;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  private String name;
  private int age;
  private String gender;

}
