package com.doodream.data.model.air;

public enum KoreanProvince {
    Seoul("Seoul"),
    Pusan("Pusan"),
    Daegu("Daegu"),
    Incheon("Incheon"),
    GwangJu("GwangJu"),
    Daejeon("Daejeon"),
    Ulsan("Ulsan"),
    KyungKi("KyungKi"),
    KangWon("KangWon"),
    NorthChung("NorthChung"),
    SouthChung("SouthChung"),
    NorthJeon("NorthJeon"),
    SouthJeon("SouthJeon"),
    Sejong("Sejong"),
    NorthKyung("NorthKyung"),
    SouthKyung("SouthKyung"),
    Jeju("Jeju");

    final private String name;
    KoreanProvince(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
