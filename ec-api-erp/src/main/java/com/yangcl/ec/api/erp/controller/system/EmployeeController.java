package com.yangcl.ec.api.erp.controller.system;

import com.github.pagehelper.PageInfo;
import com.yangcl.ec.api.erp.common.AuthPassport;
import com.yangcl.ec.api.erp.service.erp.EmployeeService;
import com.yangcl.ec.common.entity.common.JsonResult;
import com.yangcl.ec.common.entity.erp.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
//@CrossOrigin(origins = "http://localhost:8080",maxAge = 3600)
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping(value = "/employee")
    public Employee getEmployee(long sysno){
        return employeeService.get(sysno);
    }

    @AuthPassport
    @GetMapping(value = "/employees")
    public JsonResult<Employee> getEmployeeList(@RequestParam(value = "employeename",required = false) String employeeName,
                                                @RequestParam(value = "employeesex",required = false) Integer employeeSex,
                                                @RequestParam(value = "pageNo",required = false) Integer pageNo,
                                                @RequestParam(value = "pageSize",required = false) Integer pageSize){
        pageNo=pageNo==null?1:pageNo;
        pageSize=pageSize==null?10:pageSize;
        Map<String,Object> where=new HashMap<String,Object>();
        where.put("pageNo",pageNo);
        where.put("pageSize",pageSize);
        if(employeeName!=null && employeeName!=""){
            where.put("employeeName",employeeName);
        }
        if(employeeSex!=null){
            where.put("employeeSex",employeeSex);
        }
        PageInfo<Employee> pageInfo=employeeService.select(where);
        JsonResult<Employee> rowsResult=new JsonResult<Employee>();
        rowsResult.setCode("200");
        rowsResult.setMessage("查询成功");
        rowsResult.setToken("");
        rowsResult.setTotal(pageInfo.getTotal());
        rowsResult.setRows(pageInfo.getList());
        return rowsResult;
    }
}
