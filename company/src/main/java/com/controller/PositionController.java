package com.controller;

import com.dao.CompanyRepository;
import com.dao.PositionRepository;
import com.models.Company;
import com.models.Position;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by parth on 5/13/2017.
 */
@Controller
public class PositionController
{
    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    PositionRepository positionRepository;

    @RequestMapping(value = "/postjob",method = RequestMethod.GET)
    public String loadPostJob(Model model)
    {
        Position position= new Position();
        model.addAttribute("position", position);

        return "postjob";
    }

    @RequestMapping(value = "/postjob",method = RequestMethod.POST)
    public String addNewPosition(@ModelAttribute("position") Position position,
                                 HttpSession session)
    {
        System.out.print("in post job: "+position.getLocation());
        Company company = (Company)session.getAttribute("company");
        position.setCompany(company);
        positionRepository.save(position);
        return "redirect:/viewjobs";
    }

    @RequestMapping(value = "/updatePosition/{id}",method = RequestMethod.GET)
    public String loadUpdatePosition(@PathVariable Long id,
                                     Model model)
    {
        Position position=positionRepository.findOne(id);
        model.addAttribute("position",position);
        return "updatePosting";
    }

    @RequestMapping(value = "/updatePosition",method = RequestMethod.POST)
    public String loadUpdatePosition(@ModelAttribute("position") Position position)
    {
        Position updatePosition=positionRepository.findOne(position.getId());
        updatePosition.setDescription(position.getDescription());
        updatePosition.setLocation(position.getLocation());
        updatePosition.setResponsibilities(position.getResponsibilities());
        updatePosition.setSalary(position.getSalary());
        updatePosition.setTitle(position.getTitle());
        positionRepository.save(updatePosition);
        return "redirect:/viewjobs";//should redirect to posted jobs page
    }

    @RequestMapping(value = "/viewjobs",method = RequestMethod.GET)
    public String viewPositions(Model model, HttpSession session)
    {
        if(session.getAttribute("company")!=null) {
            Company company_id = (Company) session.getAttribute("company");
            Company company = companyRepository.findOne((long) company_id.getId());//get company id from session
            List<Position> positionList = company.getPositions();
            model.addAttribute("positionList", positionList);
            ArrayList<Boolean> filters=new ArrayList<>();
            filters.add(false);
            filters.add(false);
            filters.add(false);
            model.addAttribute("filters",filters);
            return "viewjobs";
        }
        else
            return "login";
    }

    @RequestMapping(value = "/viewjobs",method = RequestMethod.POST)
    public String filterViewPositions(HttpSession session,
                                      Model model,
                                      @RequestParam(required = false) Boolean open,
                                      @RequestParam(required = false) Boolean cancelled,
                                      @RequestParam(required = false) Boolean filled)
    {
        if(session.getAttribute("company")!=null)
        {
            Company company_id = (Company) session.getAttribute("company");
            Company company = companyRepository.findOne((long) company_id.getId());
            List<Position> positionList=new ArrayList<Position>();
            List<Boolean> filters=new ArrayList<>();
            if(open==null && cancelled==null && filled==null)
            {
                positionList=company.getPositions();
                filters.add(false);
                filters.add(false);
                filters.add(false);
                model.addAttribute("filters",filters);
                model.addAttribute("positionList",positionList);

                return "viewjobs";
            }
            if(open!=null)
            {
                filters.add(true);
                positionList.addAll(positionRepository.findByStatusAndCompany(0,company));
            }
            else
                filters.add(false);

            if(cancelled!=null)
            {
                filters.add(true);
                positionList.addAll(positionRepository.findByStatusAndCompany(2,company));
            }
            else
                filters.add(false);
            if(filled!=null)
            {
                filters.add(true);
                positionList.addAll(positionRepository.findByStatusAndCompany(1,company));
            }
            else
                filters.add(false);
            model.addAttribute("filters",filters);
            model.addAttribute("positionList",positionList);
            return "viewjobs";
        }
        else
        {
            return "login";
        }
    }


    @RequestMapping(value = "/position/{id}", method = RequestMethod.GET)
    public String viewJob(@PathVariable long id,Model model,HttpSession session)
    {
        if(session.getAttribute("company")!=null)
        {
            Company company=(Company) session.getAttribute("company");
            List<Position> positionList=company.getPositions();
            Position position = positionRepository.findOne(id);

            if(checkIfPositionBelongsToCompany(position,company))
            {
                model.addAttribute("position", position);
                return "job";
            }
            else
            {
                return "errorpage";
            }
        }
        else
            return "login";
    }


    private boolean checkIfPositionBelongsToCompany(Position position,Company company)
    {
        boolean flag=false;
        List<Position> positionList=company.getPositions();
        for (Position temp:positionList)
        {
            if(temp.getId()==position.getId())
                flag=true;
        }
        return flag;
    }
}