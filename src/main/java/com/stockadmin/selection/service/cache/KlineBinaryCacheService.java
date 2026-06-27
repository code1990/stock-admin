package com.stockadmin.selection.service.cache;

import com.stockadmin.common.BusinessException;
import com.stockadmin.selection.domain.RealtimeQuoteSnapshot;
import com.stockadmin.selection.domain.Stock60MinKlineRow;
import com.stockadmin.selection.domain.Stock60MinPoolEntry;
import com.stockadmin.selection.domain.Stock60MinQuoteSnapshot;
import com.stockadmin.selection.domain.StockDailyKlineRow;
import com.stockadmin.selection.domain.StockInfo;
import com.stockadmin.selection.dto.StockKlineCachePrepareResponse;
import com.stockadmin.selection.service.engine.Stock60MinQuoteMergeService;
import com.stockadmin.selection.service.engine.StockDailyQuoteMergeService;
import com.stockadmin.selection.service.query.Stock60MinDailyKlineQueryService;
import com.stockadmin.selection.service.query.Stock60MinPoolQueryService;
import com.stockadmin.selection.service.query.Stock60MinQuoteQueryService;
import com.stockadmin.selection.service.query.StockDailyKlineQueryService;
import com.stockadmin.selection.service.query.StockPoolQueryService;
import com.stockadmin.selection.service.query.StockSelectionQuoteQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KlineBinaryCacheService
{
    private static final String MAGIC = "STOCK_ADMIN_KLINE_CACHE";
    private static final int VERSION = 1;
    private static final int BATCH_SIZE = 200;
    private static final String PERIOD_240 = "240min";
    private static final String PERIOD_60 = "60min";

    private final Path cacheDir;
    private final StockPoolQueryService stockPoolQueryService;
    private final StockDailyKlineQueryService stockDailyKlineQueryService;
    private final StockSelectionQuoteQueryService stockSelectionQuoteQueryService;
    private final StockDailyQuoteMergeService stockDailyQuoteMergeService;
    private final Stock60MinPoolQueryService stock60MinPoolQueryService;
    private final Stock60MinDailyKlineQueryService stock60MinDailyKlineQueryService;
    private final Stock60MinQuoteQueryService stock60MinQuoteQueryService;
    private final Stock60MinQuoteMergeService stock60MinQuoteMergeService;

    public KlineBinaryCacheService(@Value("${stock-admin.selection.kline-cache-dir:runtime/kline-cache}") String cacheDir,
                                   StockPoolQueryService stockPoolQueryService,
                                   StockDailyKlineQueryService stockDailyKlineQueryService,
                                   StockSelectionQuoteQueryService stockSelectionQuoteQueryService,
                                   StockDailyQuoteMergeService stockDailyQuoteMergeService,
                                   Stock60MinPoolQueryService stock60MinPoolQueryService,
                                   Stock60MinDailyKlineQueryService stock60MinDailyKlineQueryService,
                                   Stock60MinQuoteQueryService stock60MinQuoteQueryService,
                                   Stock60MinQuoteMergeService stock60MinQuoteMergeService)
    {
        this.cacheDir = Paths.get(cacheDir);
        this.stockPoolQueryService = stockPoolQueryService;
        this.stockDailyKlineQueryService = stockDailyKlineQueryService;
        this.stockSelectionQuoteQueryService = stockSelectionQuoteQueryService;
        this.stockDailyQuoteMergeService = stockDailyQuoteMergeService;
        this.stock60MinPoolQueryService = stock60MinPoolQueryService;
        this.stock60MinDailyKlineQueryService = stock60MinDailyKlineQueryService;
        this.stock60MinQuoteQueryService = stock60MinQuoteQueryService;
        this.stock60MinQuoteMergeService = stock60MinQuoteMergeService;
    }

    public boolean dailyCacheExists()
    {
        return Files.isRegularFile(dailyCachePath());
    }

    public boolean sixtyMinCacheExists()
    {
        return Files.isRegularFile(sixtyMinCachePath());
    }

    public StockKlineCachePrepareResponse prepareDaily(Integer requestedTradeDate)
    {
        RealtimeQuoteSnapshot quoteSnapshot = stockSelectionQuoteQueryService.queryRealtimeQuoteSnapshot();
        Integer latestDailyTradeDate = stockDailyKlineQueryService.findLatestTradeDate();
        Integer targetTradeDate = resolveTargetTradeDate(requestedTradeDate, latestDailyTradeDate, quoteSnapshot == null ? null : Integer.valueOf(quoteSnapshot.getLatestTradeDate()), "t_stock_daily_240 or t_stock_quote");
        boolean includeQuote = shouldIncludeIntradayQuote() && quoteSnapshot != null && quoteSnapshot.getLatestTradeDate() >= targetTradeDate.intValue();
        List<StockInfo> stocks = stockPoolQueryService.queryStocks(null);
        writeDailyCache(stocks, targetTradeDate, includeQuote ? quoteSnapshot : null);
        return buildResponse(PERIOD_240, targetTradeDate, stocks.size(), dailyCachePath(), includeQuote);
    }

    public StockKlineCachePrepareResponse prepareSixtyMin(Integer requestedTradeDate)
    {
        List<Stock60MinPoolEntry> poolEntries = stock60MinPoolQueryService.queryPoolEntries("", null);
        List<StockInfo> stocks = stock60MinPoolQueryService.toStockInfos(poolEntries);
        Integer latestDailyTradeDate = stock60MinDailyKlineQueryService.findLatestTradeDate();
        Integer latestQuoteTradeDate = stock60MinQuoteQueryService.findLatestTradeDate();
        Integer targetTradeDate = resolveTargetTradeDate(requestedTradeDate, latestDailyTradeDate, latestQuoteTradeDate, "t_stock_daily_60 or t_stock_quote_60");
        List<String> stockCodes = toStockCodes(stocks);
        boolean includeQuote = shouldIncludeIntradayQuote() && latestQuoteTradeDate != null && latestQuoteTradeDate.intValue() >= targetTradeDate.intValue();
        Stock60MinQuoteSnapshot quoteSnapshot = includeQuote ? stock60MinQuoteQueryService.querySnapshot(stockCodes, targetTradeDate) : null;
        writeSixtyMinCache(stocks, targetTradeDate, quoteSnapshot);
        return buildResponse(PERIOD_60, targetTradeDate, stocks.size(), sixtyMinCachePath(), includeQuote);
    }

    public List<StockDailyKlineRow> readDailyRows(String stockCode)
    {
        return readRows(stockCode, dailyCachePath(), true).dailyRows;
    }

    public List<StockDailyKlineRow> loadDailyRows(String stockCode, Integer tradeDate)
    {
        if (!dailyCacheExists())
        {
            prepareDaily(tradeDate);
        }
        return readDailyRows(stockCode);
    }

    public List<Stock60MinKlineRow> readSixtyMinRows(String stockCode)
    {
        return readRows(stockCode, sixtyMinCachePath(), false).sixtyMinRows;
    }

    public List<Stock60MinKlineRow> loadSixtyMinRows(String stockCode, Integer tradeDate)
    {
        if (!sixtyMinCacheExists())
        {
            prepareSixtyMin(tradeDate);
        }
        return readSixtyMinRows(stockCode);
    }

    public Integer readDailyTradeDate()
    {
        return readHeader(dailyCachePath()).tradeDate;
    }

    public Integer readSixtyMinTradeDate()
    {
        return readHeader(sixtyMinCachePath()).tradeDate;
    }

    private void writeDailyCache(List<StockInfo> stocks, Integer targetTradeDate, RealtimeQuoteSnapshot quoteSnapshot)
    {
        List<CacheEntry> entries = new ArrayList<CacheEntry>();
        Path dataPath = cacheDir.resolve("daily_240.data.tmp");
        ensureCacheDir();
        try
        {
            long dataOffset = 0L;
            DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dataPath.toFile())));
            try
            {
                for (int start = 0; start < stocks.size(); start += BATCH_SIZE)
                {
                    List<StockInfo> batch = stocks.subList(start, Math.min(start + BATCH_SIZE, stocks.size()));
                    List<String> stockCodes = toStockCodes(batch);
                    Map<String, List<StockDailyKlineRow>> rowsByStock = groupDailyRows(stockDailyKlineQueryService.queryByStockCodesAndTradeDate(stockCodes, targetTradeDate));
                    for (StockInfo stock : batch)
                    {
                        List<StockDailyKlineRow> rows = rowsByStock.get(stock.getCode());
                        if (rows == null || rows.isEmpty())
                        {
                            continue;
                        }
                        List<StockDailyKlineRow> mergedRows = stockDailyQuoteMergeService.merge(stock, rows, quoteSnapshot);
                        byte[] block = serializeDailyBlock(stock, mergedRows);
                        dataOut.write(block);
                        entries.add(new CacheEntry(stock.getCode(), dataOffset, block.length));
                        dataOffset += block.length;
                    }
                }
            }
            finally
            {
                dataOut.close();
            }
            writeFinalCache(PERIOD_240, targetTradeDate, entries, dataPath, dailyCachePath());
        }
        catch (IOException ex)
        {
            throw new BusinessException("write 240min kline cache failed", ex);
        }
        finally
        {
            deleteQuietly(dataPath);
        }
    }

    private void writeSixtyMinCache(List<StockInfo> stocks, Integer targetTradeDate, Stock60MinQuoteSnapshot quoteSnapshot)
    {
        List<CacheEntry> entries = new ArrayList<CacheEntry>();
        Path dataPath = cacheDir.resolve("daily_60.data.tmp");
        ensureCacheDir();
        try
        {
            long dataOffset = 0L;
            DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dataPath.toFile())));
            try
            {
                for (int start = 0; start < stocks.size(); start += BATCH_SIZE)
                {
                    List<StockInfo> batch = stocks.subList(start, Math.min(start + BATCH_SIZE, stocks.size()));
                    List<String> stockCodes = toStockCodes(batch);
                    Map<String, List<Stock60MinKlineRow>> rowsByStock = groupSixtyMinRows(stock60MinDailyKlineQueryService.queryByStockCodesAndTradeDate(stockCodes, targetTradeDate));
                    for (StockInfo stock : batch)
                    {
                        List<Stock60MinKlineRow> rows = rowsByStock.get(stock.getCode());
                        if (rows == null || rows.isEmpty())
                        {
                            continue;
                        }
                        List<Stock60MinKlineRow> mergedRows = stock60MinQuoteMergeService.merge(stock, rows, quoteSnapshot);
                        byte[] block = serializeSixtyMinBlock(stock, mergedRows);
                        dataOut.write(block);
                        entries.add(new CacheEntry(stock.getCode(), dataOffset, block.length));
                        dataOffset += block.length;
                    }
                }
            }
            finally
            {
                dataOut.close();
            }
            writeFinalCache(PERIOD_60, targetTradeDate, entries, dataPath, sixtyMinCachePath());
        }
        catch (IOException ex)
        {
            throw new BusinessException("write 60min kline cache failed", ex);
        }
        finally
        {
            deleteQuietly(dataPath);
        }
    }

    private void writeFinalCache(String period, Integer tradeDate, List<CacheEntry> entries, Path dataPath, Path finalPath) throws IOException
    {
        Collections.sort(entries, new Comparator<CacheEntry>()
        {
            @Override
            public int compare(CacheEntry left, CacheEntry right)
            {
                return left.stockCode.compareTo(right.stockCode);
            }
        });
        byte[] header = buildHeader(period, tradeDate, entries, 0L);
        header = buildHeader(period, tradeDate, entries, header.length);
        Path tmpPath = finalPath.resolveSibling(finalPath.getFileName().toString() + ".tmp");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tmpPath.toFile())));
        try
        {
            out.write(header);
            FileInputStream in = new FileInputStream(dataPath.toFile());
            try
            {
                byte[] buffer = new byte[1024 * 1024];
                int len;
                while ((len = in.read(buffer)) >= 0)
                {
                    out.write(buffer, 0, len);
                }
            }
            finally
            {
                in.close();
            }
        }
        finally
        {
            out.close();
        }
        Files.move(tmpPath, finalPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private byte[] buildHeader(String period, Integer tradeDate, List<CacheEntry> entries, long dataBaseOffset) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeUTF(MAGIC);
        out.writeInt(VERSION);
        out.writeUTF(period);
        out.writeInt(tradeDate == null ? 0 : tradeDate.intValue());
        out.writeLong(System.currentTimeMillis());
        out.writeInt(entries.size());
        for (CacheEntry entry : entries)
        {
            out.writeUTF(entry.stockCode);
            out.writeLong(dataBaseOffset + entry.dataOffset);
            out.writeInt(entry.length);
        }
        out.flush();
        return bytes.toByteArray();
    }

    private CacheReadResult readRows(String stockCode, Path filePath, boolean daily)
    {
        CacheHeader header = readHeader(filePath);
        CacheEntry entry = header.entries.get(stockCode);
        if (entry == null)
        {
            return new CacheReadResult();
        }
        try
        {
            RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r");
            try
            {
                byte[] block = new byte[entry.length];
                file.seek(entry.dataOffset);
                file.readFully(block);
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(block));
                return daily ? deserializeDailyBlock(in) : deserializeSixtyMinBlock(in);
            }
            finally
            {
                file.close();
            }
        }
        catch (IOException ex)
        {
            throw new BusinessException("read kline cache failed: " + filePath, ex);
        }
    }

    private CacheHeader readHeader(Path filePath)
    {
        if (!Files.isRegularFile(filePath))
        {
            throw new BusinessException("kline cache file not found: " + filePath);
        }
        try
        {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath.toFile())));
            try
            {
                String magic = in.readUTF();
                if (!MAGIC.equals(magic))
                {
                    throw new BusinessException("invalid kline cache magic: " + filePath);
                }
                int version = in.readInt();
                if (version != VERSION)
                {
                    throw new BusinessException("unsupported kline cache version: " + version);
                }
                CacheHeader header = new CacheHeader();
                header.period = in.readUTF();
                header.tradeDate = Integer.valueOf(in.readInt());
                header.createdAt = Long.valueOf(in.readLong());
                int count = in.readInt();
                for (int i = 0; i < count; i++)
                {
                    String stockCode = in.readUTF();
                    long offset = in.readLong();
                    int length = in.readInt();
                    header.entries.put(stockCode, new CacheEntry(stockCode, offset, length));
                }
                return header;
            }
            finally
            {
                in.close();
            }
        }
        catch (EOFException ex)
        {
            throw new BusinessException("broken kline cache file: " + filePath, ex);
        }
        catch (IOException ex)
        {
            throw new BusinessException("read kline cache header failed: " + filePath, ex);
        }
    }

    private byte[] serializeDailyBlock(StockInfo stock, List<StockDailyKlineRow> rows) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeUTF(nullToEmpty(stock.getCode()));
        out.writeUTF(nullToEmpty(stock.getStockName()));
        out.writeUTF(nullToEmpty(stock.getMarketCode()));
        out.writeInt(rows.size());
        for (StockDailyKlineRow row : rows)
        {
            out.writeLong(row.getTradeDate() == null ? 0L : row.getTradeDate().longValue());
            writeDouble(out, row.getOpen());
            writeDouble(out, row.getHigh());
            writeDouble(out, row.getLow());
            writeDouble(out, row.getClose());
            writeDouble(out, row.getVol());
            writeDouble(out, row.getAmount());
            writeDouble(out, row.getPercent());
            writeDouble(out, row.getPreClose());
        }
        out.flush();
        return bytes.toByteArray();
    }

    private byte[] serializeSixtyMinBlock(StockInfo stock, List<Stock60MinKlineRow> rows) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeUTF(nullToEmpty(stock.getCode()));
        out.writeUTF(nullToEmpty(stock.getStockName()));
        out.writeUTF(nullToEmpty(stock.getMarketCode()));
        out.writeInt(rows.size());
        for (Stock60MinKlineRow row : rows)
        {
            out.writeLong(row.getTradeDate() == null ? 0L : row.getTradeDate().longValue());
            writeDouble(out, row.getOpen());
            writeDouble(out, row.getHigh());
            writeDouble(out, row.getLow());
            writeDouble(out, row.getClose());
            writeDouble(out, row.getVol());
            writeDouble(out, row.getAmount());
            writeDouble(out, row.getPercent());
            writeDouble(out, row.getPreClose());
        }
        out.flush();
        return bytes.toByteArray();
    }

    private CacheReadResult deserializeDailyBlock(DataInputStream in) throws IOException
    {
        CacheReadResult result = new CacheReadResult();
        String stockCode = in.readUTF();
        String stockName = in.readUTF();
        in.readUTF();
        int size = in.readInt();
        for (int i = 0; i < size; i++)
        {
            StockDailyKlineRow row = new StockDailyKlineRow();
            row.setStockCode(stockCode);
            row.setStockName(stockName);
            row.setTradeDate(Integer.valueOf((int) in.readLong()));
            row.setOpen(readDouble(in));
            row.setHigh(readDouble(in));
            row.setLow(readDouble(in));
            row.setClose(readDouble(in));
            row.setVol(readDouble(in));
            row.setAmount(readDouble(in));
            row.setPercent(readDouble(in));
            row.setPreClose(readDouble(in));
            result.dailyRows.add(row);
        }
        return result;
    }

    private CacheReadResult deserializeSixtyMinBlock(DataInputStream in) throws IOException
    {
        CacheReadResult result = new CacheReadResult();
        String stockCode = in.readUTF();
        String stockName = in.readUTF();
        in.readUTF();
        int size = in.readInt();
        for (int i = 0; i < size; i++)
        {
            Stock60MinKlineRow row = new Stock60MinKlineRow();
            row.setStockCode(stockCode);
            row.setStockName(stockName);
            row.setTradeDate(Long.valueOf(in.readLong()));
            row.setOpen(readDouble(in));
            row.setHigh(readDouble(in));
            row.setLow(readDouble(in));
            row.setClose(readDouble(in));
            row.setVol(readDouble(in));
            row.setAmount(readDouble(in));
            row.setPercent(readDouble(in));
            row.setPreClose(readDouble(in));
            result.sixtyMinRows.add(row);
        }
        return result;
    }

    private void writeDouble(DataOutputStream out, Double value) throws IOException
    {
        out.writeDouble(value == null ? Double.NaN : value.doubleValue());
    }

    private Double readDouble(DataInputStream in) throws IOException
    {
        double value = in.readDouble();
        return Double.isNaN(value) ? null : Double.valueOf(value);
    }

    private Map<String, List<StockDailyKlineRow>> groupDailyRows(List<StockDailyKlineRow> rows)
    {
        Map<String, List<StockDailyKlineRow>> rowsByStock = new HashMap<String, List<StockDailyKlineRow>>();
        for (StockDailyKlineRow row : rows)
        {
            if (row == null || row.getStockCode() == null)
            {
                continue;
            }
            List<StockDailyKlineRow> stockRows = rowsByStock.get(row.getStockCode());
            if (stockRows == null)
            {
                stockRows = new ArrayList<StockDailyKlineRow>();
                rowsByStock.put(row.getStockCode(), stockRows);
            }
            stockRows.add(row);
        }
        return rowsByStock;
    }

    private Map<String, List<Stock60MinKlineRow>> groupSixtyMinRows(List<Stock60MinKlineRow> rows)
    {
        Map<String, List<Stock60MinKlineRow>> rowsByStock = new HashMap<String, List<Stock60MinKlineRow>>();
        for (Stock60MinKlineRow row : rows)
        {
            if (row == null || row.getStockCode() == null)
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
        return rowsByStock;
    }

    private Integer resolveTargetTradeDate(Integer requestedTradeDate, Integer latestDailyTradeDate, Integer latestQuoteTradeDate, String sourceName)
    {
        if (requestedTradeDate != null)
        {
            return requestedTradeDate;
        }
        int latestDaily = latestDailyTradeDate == null ? 0 : latestDailyTradeDate.intValue();
        int latestQuote = latestQuoteTradeDate == null ? 0 : latestQuoteTradeDate.intValue();
        int resolved = Math.max(latestDaily, latestQuote);
        if (resolved <= 0)
        {
            throw new BusinessException("no available trade date found from " + sourceName);
        }
        return Integer.valueOf(resolved);
    }

    private boolean shouldIncludeIntradayQuote()
    {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
        {
            return false;
        }
        LocalTime time = now.toLocalTime();
        return !time.isBefore(LocalTime.of(9, 25)) && !time.isAfter(LocalTime.of(15, 5));
    }

    private StockKlineCachePrepareResponse buildResponse(String period, Integer tradeDate, int stockCount, Path filePath, boolean includeQuote)
    {
        StockKlineCachePrepareResponse response = new StockKlineCachePrepareResponse();
        response.setPeriod(period);
        response.setTradeDate(tradeDate);
        response.setStockCount(Integer.valueOf(stockCount));
        response.setFilePath(filePath.toAbsolutePath().toString());
        response.setIncludeQuote(Boolean.valueOf(includeQuote));
        return response;
    }

    private List<String> toStockCodes(List<StockInfo> stocks)
    {
        List<String> stockCodes = new ArrayList<String>();
        for (StockInfo stock : stocks)
        {
            if (stock != null && stock.getCode() != null && stock.getCode().trim().length() > 0)
            {
                stockCodes.add(stock.getCode());
            }
        }
        return stockCodes;
    }

    private Path dailyCachePath()
    {
        return cacheDir.resolve("kline_240.bin");
    }

    private Path sixtyMinCachePath()
    {
        return cacheDir.resolve("kline_60.bin");
    }

    private void ensureCacheDir()
    {
        try
        {
            Files.createDirectories(cacheDir);
        }
        catch (IOException ex)
        {
            throw new BusinessException("create kline cache dir failed: " + cacheDir, ex);
        }
    }

    private void deleteQuietly(Path path)
    {
        try
        {
            Files.deleteIfExists(path);
        }
        catch (IOException ignored)
        {
        }
    }

    private String nullToEmpty(String value)
    {
        return value == null ? "" : value;
    }

    private static class CacheEntry
    {
        private final String stockCode;
        private final long dataOffset;
        private final int length;

        private CacheEntry(String stockCode, long dataOffset, int length)
        {
            this.stockCode = stockCode;
            this.dataOffset = dataOffset;
            this.length = length;
        }
    }

    private static class CacheHeader
    {
        private String period;
        private Integer tradeDate;
        private Long createdAt;
        private final Map<String, CacheEntry> entries = new LinkedHashMap<String, CacheEntry>();
    }

    private static class CacheReadResult
    {
        private final List<StockDailyKlineRow> dailyRows = new ArrayList<StockDailyKlineRow>();
        private final List<Stock60MinKlineRow> sixtyMinRows = new ArrayList<Stock60MinKlineRow>();
    }
}
