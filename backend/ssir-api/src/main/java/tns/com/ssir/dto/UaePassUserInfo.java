package tns.com.ssir.dto;

import lombok.Data;

@Data
public class UaePassUserInfo {
    private String sub;
    private String uuid;
    private String fullnameEN;
    private String firstnameEN;
    private String lastnameEN;
    private String email;
    private String mobile;
    private String idn;
}