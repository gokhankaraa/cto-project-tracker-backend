package com.kolaysoft.ctotracker.dto.filter;

import java.util.List;
import java.util.Objects;

/** Filtre kayıtlarının ortak normalleştirme kuralları (filtre sözleşmesi T13, bölüm 1). */
final class FilterValues {

    private FilterValues() {
    }

    /**
     * Çok değerli bir parametreyi null içermeyen, tekrarsız ve değiştirilemez bir listeye çevirir.
     * Parametre verilmemişse boş liste döner; boş liste "filtre yok" demektir.
     *
     * <p>Null'ların elenmesi gerekli: {@code ?riskLevel=ORTA,} gibi bir istekte Spring sondaki boş
     * parçayı enum'a çevirirken null üretir.
     */
    static <E> List<E> values(List<E> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull).distinct().toList();
    }

    /** Boş veya yalnızca boşluktan oluşan metni "verilmemiş" (null) sayar, kalan metni kırpar. */
    static String text(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
