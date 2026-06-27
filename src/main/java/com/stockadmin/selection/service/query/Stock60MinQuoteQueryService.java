package com.stockadmin.selection.service.query;

import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.Stock60MinQuoteSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Stock60MinQuoteQueryService
{
    private static final Logger log = LoggerFactory.getLogger(Stock60MinQuoteQueryService.class);
    private static final int SQLITE_IN_BATCH_SIZE = 800;

    private final JdbcTemplate stock60MinQuoteJdbcTemplate;

    public Stock60MinQuoteQueryService(@Qualifier("stock60MinQuoteJdbcTemplate") JdbcTemplate stock60MinQuoteJdbcTemplate)
    {
        this.stock60MinQuoteJdbcTemplate = stock60MinQuoteJdbcTemplate;
    }

    public Integer findLatestTradeDate()
    {
        Long latestTradeDate;
        try
        {
            latestTradeDate = stock60MinQuoteJdbcTemplate.queryForObject("SELECT MAX(trade_time) FROM t_stock_quote_60", Long.class);
        }
        catch (DataAccessException ex)
        {
            log.warn("Query t_stock_quote_60 latest trade time failed, continue without 60min intraday quote. message={}", ex.getMessage());
            return null;
        }
        if (latestTradeDate == null || latestTradeDate.longValue() <= 0L)
        {
            return null;
        }
        return Integer.valueOf((int) (latestTradeDate.longValue() / 10000L));
    }

    public Stock60MinQuoteSnapshot querySnapshot(List<String> stockCodes, Integer tradeDate)
    {
        if (stockCodes == null || stockCodes.isEmpty() || tradeDate == null)
        {
            return new Stock60MinQuoteSnapshot(Collections.<String, List<Stock60MinKlineRow>>emptyMap(), 0);
        }

        long startTradeDateInclusive = tradeDate.intValue() * 10000L;
        long endTradeDateExclusive = (tradeDate.intValue() + 1L) * 10000L;
        List<Stock60MinKlineRow> rows = queryRows(stockCodes, startTradeDateInclusive, endTradeDateExclusive);

        Map<String, List<Stock60MinKlineRow>> rowsByStock = new LinkedHashMap<String, List<Stock60MinKlineRow>>();
        for (Stock60MinKlineRow row : rows)
        {
            if (row == null || !hasText(row.getStockCode()))
            {
                continue;
            }
            List<Stock60MinKlineRow> stockRows = rowsByStock.get(row.getStockCode());
            if (stockRows == null)
            {
                stockRows = new ArrayList<Stock60MinKlineRow>();
                rowsByStock.put(row.getStockCode(), stockRows);
            }
            stockRows.add(row);
        }
        return new Stock60MinQuoteSnapshot(rowsByStock, rowsByStock.isEmpty() ? 0 : tradeDate.intValue());
    }

    private List<Stock60MinKlineRow> queryRows(List<String> stockCodes, long startTradeDateInclusive, long endTradeDateExclusive)
    {
        List<Stock60MinKlineRow> rows = new ArrayList<Stock60MinKlineRow>();
        for (int start = 0; start < stockCodes.size(); start += SQLITE_IN_BATCH_SIZE)
        {
            int end = Math.min(start + SQLITE_IN_BATCH_SIZE, stockCodes.size());
            List<String> codeBatch = stockCodes.subList(start, end);
            List<Object> args = new ArrayList<Object>(codeBatch.size() + 2);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT stock_code, stock_name, trade_time AS trade_date, open, high, low, close, vol, amount, percent, pre_close ");
            sql.append("FROM t_stock_quote_60 WHERE stock_code IN (");
            for (int i = 0; i < codeBatch.size(); i++)
            {
                if (i > 0)
                {
                    sql.append(',');
                }
                sql.append('?');
                args.add(codeBatch.get(i));
            }
            sql.append(") AND trade_time >= ? AND trade_time < ? ORDER BY stock_code, trade_time");
            args.add(Long.valueOf(startTradeDateInclusive));
            args.add(Long.valueOf(endTradeDateExclusive));
            try
            {
                rows.addAll(stock60MinQuoteJdbcTemplate.query(sql.toString(), args.toArray(), new Stock60MinKlineRowMapper()));
            }
            catch (DataAccessException ex)
            {
                log.warn("Query t_stock_quote_60 failed, return partial quote rows. start={}, end={}, message={}",
                        Integer.valueOf(start), Integer.valueOf(end), ex.getMessage());
                return rows;
            }
        }
        return rows;
    }

    private static class Stock60MinKlineRowMapper implements RowMapper<Stock60MinKlineRow>
    {
        @Override
        public Stock60MinKlineRow mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            Stock60MinKlineRow row = new Stock60MinKlineRow();
            row.setStockCode(rs.getString("stock_code"));
            row.setStockName(rs.getString("stock_name"));
            row.setTradeDate(Long.valueOf(rs.getLong("trade_date")));
            row.setOpen(getDouble(rs, "open"));
            row.setHigh(getDouble(rs, "high"));
            row.setLow(getDouble(rs, "low"));
            row.setClose(getDouble(rs, "close"));
            row.setVol(getDouble(rs, "vol"));
            row.setAmount(getDouble(rs, "amount"));
            row.setPercent(getDouble(rs, "percent"));
            row.setPreClose(getDouble(rs, "pre_close"));
            return row;
        }

        private static Double getDouble(ResultSet rs, String columnName) throws SQLException
        {
            double value = rs.getDouble(columnName);
            return rs.wasNull() ? null : Double.valueOf(value);
        }
    }

    private boolean hasText(String value)
    {
        return value != null && value.trim().length() > 0;
    }
}
