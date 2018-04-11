package com.doodream.data.util.serdes;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import javax.management.modelmbean.InvalidTargetObjectTypeException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class ArrayConverter {


    public static <T> List<T> filledList(Class<T> cls, int size) {
        return Observable.range(0, size)
                .map(integer -> cls.newInstance())
                .subscribeOn(Schedulers.computation())
                .toList().blockingGet();
    }

    public static <U,T extends Array> Array fromArray(Class<T> cls, U[] items) throws IllegalAccessException, InstantiationException, InvalidTargetObjectTypeException {
        Array array = cls.getAnnotation(Array.class);
        if(!array.cls().equals(items[0].getClass())) {
            throw new InvalidTargetObjectTypeException(String.format("%s is not array of type %s",cls.getCanonicalName(), items[0].getClass().getCanonicalName()));
        }
        if(array == null) {
            throw new ClassCastException(String.format("%s is not type of %s", cls.getCanonicalName(), Array.class.getTypeName()));
        }
        Field[] fields = cls.getDeclaredFields();
        T object = cls.newInstance();
        Observable<Object> valueObservable = Observable.fromArray(items);
        Observable.fromArray(fields)
                .filter(field -> field.getAnnotation(ArrayItem.class) != null)
                .sorted(ArrayConverter::byIndex).zipWith(valueObservable, (field, o) -> {
                    field.set(object,o);
                    return field;
                }).blockingSubscribe();
        return object;
    }

    public static <V, R> R[] toArray(V arrayable) {
        Class arrayCls = arrayable.getClass();
        Field[] fields = arrayCls.getDeclaredFields();
        Array array = (Array) arrayCls.getAnnotation(Array.class);
        Class itemClass = array.cls();
        return (R[]) Arrays.asList(Observable.fromArray(fields)
                .sorted(ArrayConverter::byIndex)
                .map(field -> extractValue(field, arrayable)).cast(itemClass).blockingIterable()).toArray();
    }

    private static <T> Object extractValue(Field field, T arrayable) throws IllegalAccessException {
        return field.get(arrayable);
    }

    private static int byIndex(Field field, Field field1) {
        return field.getAnnotation(ArrayItem.class).index() - field1.getAnnotation(ArrayItem.class).index() ;
    }

}
