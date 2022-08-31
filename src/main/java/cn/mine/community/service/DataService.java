package cn.mine.community.service;

import java.util.Date;

public interface DataService {
    void recordUV(String ip);
    long calculateUV(Date startDate, Date endDate);
    void recordDAU(int userId);
    long calculateDAU(Date startDate, Date endDate);
}
