package pl.kufieta.reserveit_api.facilities.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter

// To tworzy pusty konstuktor czego wymaga JPA / Hibernates bo potem ustawia pola przez refleksję
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "facilities")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    @Column(nullable = false)
    private LocalTime opensAt;

    @Column(nullable = false)
    private LocalTime closesAt;

    @Column(nullable = false)
    private boolean active = true;


    public Facility(String name, BigDecimal pricePerHour, LocalTime opensAt, LocalTime closesAt) {
        this.name = name;
        this.pricePerHour = pricePerHour;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
    }
}