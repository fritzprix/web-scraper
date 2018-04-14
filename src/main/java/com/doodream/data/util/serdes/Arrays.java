package com.doodream.data.util.serdes;

import io.reactivex.Observable;
import io.reactivex.Single;

import javax.management.modelmbean.InvalidTargetObjectTypeException;
import java.lang.reflect.Field;
import java.util.List;

public class Arrays {


    public static <U,T> T fromArray(Class<T> cls, U[] items) throws IllegalAccessException, InstantiationException, InvalidTargetObjectTypeException {
        Array array = cls.getAnnotation(Array.class);
        if(array == null) {
            throw new ClassCastException(String.format("%s is not type of %s", cls.getCanonicalName(), Array.class.getTypeName()));
        }
        if(!array.cls().equals(items[0].getClass())) {
            throw new InvalidTargetObjectTypeException(String.format("%s is not array of type %s",cls.getCanonicalName(), items[0].getClass().getCanonicalName()));
        }
        Field[] fields = cls.getDeclaredFields();
        T object = cls.newInstance();
        Observable<Object> valueObservable = Observable.fromArray(items);
        Observable.fromArray(fields)
                .filter(field -> field.getAnnotation(ArrayItem.class) != null)
                .sorted(Arrays::byIndex).zipWith(valueObservable, (field, o) -> {
                    field.set(object,o);
                    return field;
                }).blockingSubscribe();
        return object;
    }

    private static <V> Single<List<String>> asList(V object) {
        if(!object.getClass().isAnnotationPresent(Array.class))
            throw new IllegalArgumentException(String.format("Not Supported type %s", object.getClass().getCanonicalName()));
        Class arrayCls = object.getClass();
        Field[] fields = arrayCls.getDeclaredFields();
        return Observable.fromArray(fields)
                .filter(field -> field.isAnnotationPresent(ArrayItem.class))
                .sorted(Arrays::byIndex)
                .doOnNext(field -> field.setAccessible(true))
                .map(field -> extractValue(field, object))
                .toList();
    }

    public static <V> Single<String[]> asArray(V object) {
        return asList(object).map(strings -> strings.toArray(new String[0]));
    }

    private static <T> String extractValue(Field field, T array) throws IllegalAccessException {
        return String.valueOf(field.get(array)).trim();
    }

    private static int byIndex(Field field, Field field1) {
        return field.getAnnotation(ArrayItem.class).index() - field1.getAnnotation(ArrayItem.class).index() ;
    }

    private static <V> Observable<List<String>> listSingle(V object) {
        return Observable.create(emitter -> emitter.setDisposable(asList(object)
                .subscribe((strings, throwable) -> {
                    if (throwable != null) {
                        emitter.onError(throwable);
                    }
                    emitter.onNext(strings);
                    emitter.onComplete();
                })));
    }

    public static <V> Observable<String[]> arraySingle(V object) {
        return listSingle(object).map(strings -> strings.toArray(new String[0]));

    }
}
