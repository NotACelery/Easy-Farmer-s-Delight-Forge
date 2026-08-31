package dev.celerbi.easyfarmersdelightcompat.integration;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class FarmersDelightAdapter {
    private static final String CONFIGURATION_CLASS = "vectorwing.farmersdelight.common.Configuration";
    private static final String RICH_SOIL_BOOST_CHANCE_FIELD = "RICH_SOIL_BOOST_CHANCE";

    private static boolean resolved;
    private static boolean failed;
    private static Object boostChanceValue;
    private static Method boostChanceGetMethod;

    public double richSoilBoostChance() {
        resolve();
        if (failed || boostChanceValue == null) {
            return 0.0D;
        }

        try {
            Object value;
            if (boostChanceValue instanceof Supplier<?> supplier) {
                value = supplier.get();
            } else {
                value = boostChanceGetMethod.invoke(boostChanceValue);
            }
            if (value instanceof Number number) {
                return Math.max(0.0D, Math.min(1.0D, number.doubleValue()));
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            fail(e);
        }
        return 0.0D;
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> configuration = ReflectionCache.type(CONFIGURATION_CLASS);
            boostChanceValue = ReflectionCache.field(configuration, RICH_SOIL_BOOST_CHANCE_FIELD).get(null);
            if (boostChanceValue == null) {
                fail(null);
                return;
            }
            if (!(boostChanceValue instanceof Supplier<?>)) {
                boostChanceGetMethod = ReflectionCache.publicMethod(boostChanceValue.getClass(), "get");
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            fail(e);
        }
    }

    private static void fail(Throwable error) {
        if (!failed) {
            System.err.println(
                    "[Easy Farmer's Delight Compat] Could not read Farmer's Delight Rich Soil configuration; "
                            + "Rich Soil acceleration is disabled."
            );
            if (error != null) {
                error.printStackTrace();
            }
        }
        failed = true;
    }
}
