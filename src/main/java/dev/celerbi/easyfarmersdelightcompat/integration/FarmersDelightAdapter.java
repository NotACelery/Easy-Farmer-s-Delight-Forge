package dev.celerbi.easyfarmersdelightcompat.integration;

import java.lang.reflect.Field;

public final class FarmersDelightAdapter {
    private static final String CONFIGURATION_CLASS = "vectorwing.farmersdelight.common.Configuration";
    private static final String RICH_SOIL_BOOST_CHANCE_FIELD = "RICH_SOIL_BOOST_CHANCE";

    private boolean failed;

    public double richSoilBoostChance() {
        if (failed) {
            return 0.0D;
        }

        try {
            Class<?> configuration = Class.forName(CONFIGURATION_CLASS);
            Field field = configuration.getField(RICH_SOIL_BOOST_CHANCE_FIELD);
            Object supplier = field.get(null);
            Object value = supplier.getClass().getMethod("get").invoke(supplier);
            if (value instanceof Number number) {
                return Math.max(0.0D, Math.min(1.0D, number.doubleValue()));
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            fail(e);
        }
        return 0.0D;
    }

    private void fail(Exception e) {
        if (!failed) {
            System.err.println("[Easy Farmer's Delight Compat] Could not read Farmer's Delight Rich Soil configuration; Rich Soil acceleration is disabled.");
            e.printStackTrace();
        }
        failed = true;
    }
}
