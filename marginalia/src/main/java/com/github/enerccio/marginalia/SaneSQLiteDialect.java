package com.github.enerccio.marginalia;

import org.hibernate.community.dialect.SQLiteDialect;

public class SaneSQLiteDialect extends SQLiteDialect {

    // Disables CHECK (col IN ('VAL1', 'VAL2')) for String enums
    @Override
    public String getCheckCondition(String columnName, String[] values) {
        return null;
    }

    // Disables CHECK (col BETWEEN min AND max) for Ordinal enums / numeric ranges
    @Override
    public String getCheckCondition(String columnName, long min, long max) {
        return null;
    }

}
