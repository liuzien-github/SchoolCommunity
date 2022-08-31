package cn.mine.community.controller;

import cn.mine.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    @RequestMapping(value = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "site/admin/data";
    }

    @RequestMapping(value = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                              @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, Model model) {
        long uv = dataService.calculateUV(startDate, endDate);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", startDate);
        model.addAttribute("uvEndDate", endDate);
        return "forward:/data"; //相当于本Controller处理了一半，转发给另一个Controller继续处理；也可以直接交给模版处理。
    }

    @RequestMapping(value = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, Model model) {
        long dau = dataService.calculateDAU(startDate, endDate);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", startDate);
        model.addAttribute("dauEndDate", endDate);
        return "forward:/data"; //相当于本Controller处理了一半，转发给另一个Controller继续处理；也可以直接交给模版处理。
    }
}
