package com.ruoyi.web.controller.system;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ETelCodeTraceDecoder {


    public static void main(String[] args) {
        String hexData = "00FB010119E8F772AA80101A141A15AE02";
        byte[] data = hexStringToByteArray(hexData);
        Map.Entry<Integer, Integer> kv = readTimeValue(data, 4);
        float timeCost = kv.getValue() / 100f;
        System.out.println(String.format("\t总耗时:%.2fms\n", timeCost));
        System.out.println("\t行号\t\t 执行次数\t\t 耗时:\t\t   耗时占比");
        List<LineInfo> lineInfos = parse(kv.getKey(), data);
        for (LineInfo lineInfo : lineInfos) {
            if (lineInfo.count != 0) {
                lineInfo.print(timeCost);
            }
        }
    }

    public static List<LineInfo> parse(int timeBytes, byte[] data) {
        int startLine = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        int endLine = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

        int readIndex = 4 + timeBytes;
        int bitIndex = 0;

        List<LineInfo> lineInfos = new ArrayList<>();
        for (int line = startLine; line <= endLine; line++) {
            int x = (data[readIndex] >> (7 - bitIndex)) & 1;
            if (++bitIndex == 8) {
                readIndex++;
                bitIndex = 0;
            }
            if (x == 0) {
                continue;
            }
            int flag2 = (data[readIndex] >> (7 - bitIndex)) & 1;
            if (++bitIndex == 8) {
                readIndex++;
                bitIndex = 0;
            }
            lineInfos.add(new LineInfo(line, (x << 1) | flag2));
        }

        for (LineInfo lineInfo : lineInfos) {
            int x = (data[readIndex] >> (7 - bitIndex)) & 1;
            if (++bitIndex == 8) {
                readIndex++;
                bitIndex = 0;
            }
            if (x == 0) {
                int countFlag2 = (data[readIndex] >> (7 - bitIndex)) & 1;
                if (++bitIndex == 8) {
                    readIndex++;
                    bitIndex = 0;
                }
                lineInfo.count = countFlag2;
            } else {
                if (bitIndex == 0) {
                    lineInfo.count = data[readIndex++] & 0xFF;
                } else {
                    lineInfo.count = readByte(bitIndex, readIndex++, data) & 0xFF;
                }
            }
        }
        if (bitIndex != 0) {
            readIndex++;
        }
        for (LineInfo lineInfo : lineInfos) {
            if (lineInfo.flag == 3) {
                Map.Entry<Integer, Integer> values = readTimeValue(data, readIndex);
                lineInfo.timeCostInMs = values.getValue() / 100f;
                readIndex += values.getKey();
            }
        }
        return lineInfos;
    }

    private static Map.Entry<Integer, Integer> readTimeValue(byte[] data, int writeIndex) {
        int head = data[writeIndex] & 0xFF;
        int type = head >> 4, value = head & 0x0F;
        int timeInMicro, consumedBytes;
        switch (type) {
            case 0:
                timeInMicro = head & 0x0F;
                consumedBytes = 1;
                break;
            case 1:
                timeInMicro = (value << 8) | (data[writeIndex + 1] & 0xFF);
                consumedBytes = 2;
                break;
            case 2:
                timeInMicro = (value << 16) | ((data[writeIndex + 1] & 0xFF) << 8) | (data[writeIndex + 2] & 0xFF);
                consumedBytes = 3;
                break;
            case 3:
                timeInMicro = (value << 24) | ((data[writeIndex + 1] & 0xFF) << 16) |
                        ((data[writeIndex + 2] & 0xFF) << 8) | (data[writeIndex + 3] & 0xFF);
                consumedBytes = 4;
                break;
            default:
                timeInMicro = 0;
                consumedBytes = 1;
        }
        return new AbstractMap.SimpleEntry<>(consumedBytes, timeInMicro);
    }

    public static byte[] hexStringToByteArray(String hexStr) {
        int len = hexStr.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hexStr.substring(i, i + 2), 16);
        }
        return result;
    }

    public static byte readByte(int bi, int wi, byte[] data) {
        int startPos = 7 - bi;
        int highBitsCount = 8 - bi;
        int extractMask = ((1 << highBitsCount) - 1) << (startPos - highBitsCount + 1);
        int extractedHighBits = ((data[wi] & extractMask) >>> (startPos - highBitsCount + 1)) & 0xFF;
        int lowBits = ((data[wi + 1] & 0xFF) >>> (8 - bi)) & ((1 << bi) - 1);
        return (byte) ((extractedHighBits << bi) | lowBits);
    }

    public static class LineInfo {
        public int count;
        public final int flag;
        public final int lineNumber;
        public float timeCostInMs;

        public LineInfo(int lineNumber, int flag) {
            this.lineNumber = lineNumber;
            this.flag = flag;
        }

        public void print(float totalTimeCostInMs) {
            if (timeCostInMs * 100 * 100 < totalTimeCostInMs) {
                System.out.println(String.format("\t%d\t\t\t%d\t\t\t%.2fms", lineNumber, count, timeCostInMs));
            } else {
                System.out.println(String.format("\t%d\t\t\t%d\t\t\t%.2fms,  \t\t%.2f%%", lineNumber, count, timeCostInMs, timeCostInMs / totalTimeCostInMs * 100));
            }
        }
    }


}

