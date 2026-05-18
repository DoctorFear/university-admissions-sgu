package com.xettuyen2026.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.xettuyen2026.dto.TinhDiemRequest;
import com.xettuyen2026.dto.TraCuuRequest;
import com.xettuyen2026.service.WebpageService;


@Controller
public class WebpageController {

    private final WebpageService service = new WebpageService();

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute(
                "nganhList",
                service.findNganh());

        // return "index";
        return "app";
    }

    @PostMapping("/tracuu")
    public String traCuu(TraCuuRequest request, Model model) {
        
        model.addAttribute("result", service.tracuu(request));

        model.addAttribute(
                "nganhList",
                service.findNganh());
        model.addAttribute(
                "activeTab",
                "tracuu");
        model.addAttribute(
                "inputCccd",
                request.getCccd());
        model.addAttribute(
                "inputPassword",
                request.getPassword());

        // return "index";
        return "app";
    }

    @PostMapping("/vsat")
    public String tinhDiemVSAT(TinhDiemRequest request, Model model) {
        
        model.addAttribute("vsatResult", service.vsat(request));
        model.addAttribute("vsatInput", request);

        model.addAttribute(
                "nganhList",
                service.findNganh());
        model.addAttribute(
                "activeCalcMethod",
                "vsat");
        model.addAttribute(
                "activeTab",
                "tinhdiem");

        // return "index";
        return "app";
    }

    @PostMapping("/dgnl")
    public String tinhDiemDGNL(TinhDiemRequest request, Model model) {
        
        model.addAttribute("dgnlResult", service.dgnl(request));
        model.addAttribute("dgnlInput", request);

        model.addAttribute(
                "nganhList",
                service.findNganh());
        model.addAttribute(
                "activeCalcMethod",
                "dgnl");
        model.addAttribute(
                "activeTab",
                "tinhdiem");

        // return "index";
        return "app";
    }
    
    @PostMapping("/thpt")
    public String tinhDiemTHPT(TinhDiemRequest request, Model model) {

        model.addAttribute("thptResult", service.thpt(request));

        model.addAttribute("thptInput", request);

        model.addAttribute(
                "nganhList",
                service.findNganh());

        model.addAttribute(
                "activeCalcMethod",
                "thpt");

        model.addAttribute(
                "activeTab",
                "tinhdiem");

        return "app";
    }
}
