package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.RestaurantDetails;
import ar.edu.itba.paw.model.RestaurantOrderBy;
import ar.edu.itba.paw.model.RestaurantSpecialty;
import ar.edu.itba.paw.model.RestaurantTags;
import ar.edu.itba.paw.service.RestaurantService;
import ar.edu.itba.paw.service.ReviewService;
import ar.edu.itba.paw.util.PaginatedResult;
import ar.edu.itba.paw.webapp.form.FilterForm;
import ar.edu.itba.paw.webapp.form.SearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;


@Controller
public class HomeController {
    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ReviewService reviewService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(
            @ModelAttribute("searchForm") final SearchForm form
    ) {
        ModelAndView mav = new ModelAndView("home/index");

        final PaginatedResult<RestaurantDetails> results = restaurantService.search(null, 1, ControllerUtils.MAX_RESTAURANTS_FOR_HOMEPAGE, RestaurantOrderBy.RATING, true, null, null);
        mav.addObject("restaurants", results.getResult());

        return mav;
    }

    @RequestMapping(value = "/restaurants", method = RequestMethod.GET)
    public ModelAndView restaurants(
            @Valid @ModelAttribute("searchForm") final FilterForm form,
            final BindingResult errors
    ) {
        ModelAndView mav = new ModelAndView("home/restaurants");

        if (errors.hasErrors()) {
            mav.addObject("error", Boolean.TRUE);
            form.clear();
        }

        mav.addObject("specialties", RestaurantSpecialty.values());
        mav.addObject("tags", RestaurantTags.values());
        mav.addObject("order_by", RestaurantOrderBy.values());

        final PaginatedResult<RestaurantDetails> results = restaurantService.search(
                form.getSearch(),
                form.getPageOrDefault(),
                form.getSizeOrDefault(ControllerUtils.DEFAULT_SEARCH_PAGE_SIZE),
                form.getOrderByAsEnum(),
                form.getDescendingOrDefault(),
                form.getTagsAsEnums(),
                form.getSpecialtiesAsEnum()
        );
        mav.addObject("restaurants", results.getResult());
        mav.addObject("restaurantCount", results.getTotalCount());
        mav.addObject("pageCount", results.getTotalPageCount());

        return mav;
    }
}
