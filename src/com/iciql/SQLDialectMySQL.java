/*
 * Copyright 2011 James Moger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iciql;

import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;

/**
 * MySQL database dialect.
 */
public class SQLDialectMySQL extends SQLDialectDefault {

	@Override
	public String convertSqlType(String sqlType) {
		if (sqlType.equals("CLOB")) {
			return "TEXT";
		}
		return sqlType;
	}

	@Override
	public boolean supportsMemoryTables() {
		return false;
	}

	@Override
	public String prepareColumnName(String name) {
		return "`" + name + "`";
	}

	@Override
	protected boolean prepareColumnDefinition(StatementBuilder buff, boolean isAutoIncrement,
			boolean isPrimaryKey) {
		if (isAutoIncrement) {
			buff.append(" AUTO_INCREMENT");
		}
		return false;
	}

	@Override
	public void prepareCreateIndex(SQLStatement stat, String schema, String table, IndexDefinition index) {
		StatementBuilder buff = new StatementBuilder();
		buff.append("CREATE ");
		switch (index.type) {
		case STANDARD:
			break;
		case UNIQUE:
			buff.append("UNIQUE ");
			break;
		case UNIQUE_HASH:
			buff.append("UNIQUE ");
			break;
		}
		buff.append("INDEX ");
		buff.append(index.indexName);
		buff.append(" ON ");
		buff.append(table);
		buff.append("(");
		for (String col : index.columnNames) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");
		stat.setSQL(buff.toString().trim());
	}

	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName,
			TableDefinition<T> def, Object obj) {
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		buff.append(prepareTableName(schemaName, tableName)).append(" (");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(field.columnName);
		}
		buff.resetCount();
		buff.append(") VALUES (");
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append('?');
			Object value = def.getValue(obj, field);
			stat.addParameter(value);
		}
		buff.append(") ON DUPLICATE KEY UPDATE ");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(field.columnName);
			buff.append("=VALUES(");
			buff.append(field.columnName);
			buff.append(')');
		}
		stat.setSQL(buff.toString());
	}
}