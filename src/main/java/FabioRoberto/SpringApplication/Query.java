package FabioRoberto.SpringApplication;

public class Query {

	private String queryString;

	private String user;
	
	private String category; 
	
	private String relevance;

	private String expansion;

	private String expansion_should;

	public String getExpansion_should() {
		return expansion_should;
	}

	public void setExpansion_should(String expansion_should) {
		this.expansion_should = expansion_should;
	}


	public String getExpansion() {
		return expansion;
	}

	public void setExpansion(String expansion) {
		this.expansion = expansion;
	}

	public String getRelevance() {
		return relevance;
	}

	public void setRelevance(String relevance) {
		this.relevance = relevance;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}


	
}
