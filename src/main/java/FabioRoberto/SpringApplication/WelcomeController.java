package FabioRoberto.SpringApplication;
import FabioRoberto.Backend.*;
import FabioRoberto.searcher.Search;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.ArrayList;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String queryForm(Model model) {
        model.addAttribute("query", new Query());
        return "query";
    }


    @PostMapping("/query")
    public String querySubmit(@ModelAttribute Query query, Model model) {
    	Processing proc = new Processing();
    	ArrayList <Tweet> result = proc.query("Index",query.getQueryString());
    	model.addAttribute("res", result);
      return "result";
    }
    
    @PostMapping("/queryAdvanced")
    public String queryLink(Model model) {
        model.addAttribute("query", new Query());
        return "queryPersonalized";
    }

    
    @PostMapping("/queryPersonalized")
    public String querySubmit2(@ModelAttribute Query query, Model model) throws IOException {
    	if (query.getCategory()==null)
    		query.setCategory("none");
    	Processing proc = new Processing();

        Search search = new Search("Index");
        query.setExpansion_should(search.queryIndex(query.getQueryString(), false, query.getExpansion()));
        model.addAttribute("expansion", query.getExpansion_should());

        User user = new User();
    	if(!query.getUser().equals("none")){
            String path = "Users/" + query.getUser() + ".csv";
            user = proc.userFactory(path);
            ArrayList <String> preference = proc.filteredBy(user, query.getCategory());
            model.addAttribute("preference", preference);
            System.out.println(query.getQueryString());
            ArrayList <Tweet> result = proc.personalized_query("Index",query.getQueryString(), query.getExpansion_should(),user,query.getCategory(),query.getRelevance(),query.getUser());
            model.addAttribute("res", result);
            return "resultPersonalized";
        }

    	ArrayList <Tweet> result = proc.personalized_query("Index",query.getQueryString(),query.getExpansion_should(), user,query.getCategory(),query.getRelevance(),query.getUser());
    	model.addAttribute("res", result);
        return "result";
    }

}
