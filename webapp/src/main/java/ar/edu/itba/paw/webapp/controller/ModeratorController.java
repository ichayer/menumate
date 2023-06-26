package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.exception.RestaurantNotFoundException;
import ar.edu.itba.paw.model.Report;
import ar.edu.itba.paw.model.Restaurant;
import ar.edu.itba.paw.model.UserRole;
import ar.edu.itba.paw.model.UserRoleLevel;
import ar.edu.itba.paw.service.ReportService;
import ar.edu.itba.paw.service.RestaurantService;
import ar.edu.itba.paw.service.UserRoleService;
import ar.edu.itba.paw.util.MyBoolean;
import ar.edu.itba.paw.util.PaginatedResult;
import ar.edu.itba.paw.util.Pair;
import ar.edu.itba.paw.webapp.form.AddModeratorForm;
import ar.edu.itba.paw.webapp.form.DeleteModeratorForm;
import ar.edu.itba.paw.webapp.form.HandleReportForm;
import ar.edu.itba.paw.webapp.form.PagingForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
public class ModeratorController {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private RestaurantService restaurantService;

    @RequestMapping(value = "/moderators", method = RequestMethod.GET)
    public ModelAndView moderators(
            @ModelAttribute("addModeratorForm") final AddModeratorForm addModeratorForm,
            @ModelAttribute("addModeratorFormErrors") final MyBoolean addModeratorFormErrors,
            @ModelAttribute("deleteModeratorForm") final DeleteModeratorForm deleteModeratorForm,
            @Valid final PagingForm paging,
            final BindingResult errors
    ) {
        ModelAndView mav = new ModelAndView("moderator/dashboard");

        if (errors.hasErrors()) {
            mav.addObject("error", Boolean.TRUE);
            paging.clear();
        }

        List<UserRole> roles = userRoleService.getByRole(UserRoleLevel.MODERATOR);
        mav.addObject("userRoles", roles);

        PaginatedResult<Pair<Restaurant, Integer>> restaurantsWithReports = reportService.getCountByRestaurant(paging.getPageOrDefault(), paging.getSizeOrDefault(ControllerUtils.DEFAULT_MYRESTAURANTS_PAGE_SIZE));

        mav.addObject("addModeratorFormErrors", addModeratorFormErrors.get());

        mav.addObject("restaurants", restaurantsWithReports.getResult());
        mav.addObject("restaurantCount", restaurantsWithReports.getTotalCount());
        mav.addObject("pageCount", restaurantsWithReports.getTotalPageCount());

        return mav;
    }

    @RequestMapping(value = "moderators/add", method = RequestMethod.POST)
    public ModelAndView add(
            @Valid final AddModeratorForm form,
            final BindingResult errors,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            PagingForm pagingForm = new PagingForm();
            return moderators(
                    form,
                    new MyBoolean(true),
                    new DeleteModeratorForm(),
                    pagingForm,
                    new BeanPropertyBindingResult(pagingForm, "pagingForm")
            );
        }

        userRoleService.setRole(form.getEmail(), UserRoleLevel.MODERATOR);
        redirectAttributes.addFlashAttribute("addModeratorFormErrors", new MyBoolean(true));
        return new ModelAndView("redirect:/moderators");
    }

    @RequestMapping(value = "/moderators/delete", method = RequestMethod.POST)
    public ModelAndView delete(
            @Valid final DeleteModeratorForm form,
            final BindingResult errors,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            throw new IllegalStateException();
        }

        userRoleService.deleteRole(form.getUserId());
        redirectAttributes.addFlashAttribute("addModeratorFormErrors", new MyBoolean(true));
        return new ModelAndView("redirect:/moderators");
    }

    @RequestMapping(value = "/moderators/reports/{id:\\d+}", method = RequestMethod.GET)
    public ModelAndView reports(
            @PathVariable final int id,
            @ModelAttribute("handleReportForm") final HandleReportForm handleReportForm,
            @Valid final PagingForm paging,
            final BindingResult errors
    ) {
        ModelAndView mav = new ModelAndView("moderator/reports");

        if (errors.hasErrors()) {
            mav.addObject("error", Boolean.TRUE);
            paging.clear();
        }

        Restaurant restaurant = restaurantService.getById(id).orElseThrow(RestaurantNotFoundException::new);
        mav.addObject("restaurant", restaurant);

        PaginatedResult<Report> reports = reportService.getByRestaurant(id, paging.getPageOrDefault(), paging.getSizeOrDefault(ControllerUtils.DEFAULT_ORDERS_PAGE_SIZE));
        mav.addObject("reports", reports.getResult());
        mav.addObject("reportCount", reports.getTotalCount());
        mav.addObject("pageCount", reports.getTotalPageCount());

        return mav;
    }

    @RequestMapping(value = "/moderators/reports/{id:\\d+}/handle", method = RequestMethod.POST)
    public ModelAndView handleReport(
            @PathVariable final int id,
            @Valid @ModelAttribute("handleReportForm") final HandleReportForm handleReportForm,
            final BindingResult errors
    ) {
        if (errors.hasErrors()) {
            throw new IllegalStateException();
        }

        reportService.markHandled(handleReportForm.getReportId(), ControllerUtils.getCurrentUserIdOrThrow());
        return new ModelAndView(String.format("redirect:/moderators/reports/%d", id));
    }
}