package filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class AdditionalResponseHeadersFilter implements ContainerResponseFilter { 
	
	public AdditionalResponseHeadersFilter() {}
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) 
			throws IOException {
		responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
		responseContext.getHeaders().add("Access-Control-Allow-Methods", "HEAD,GET,PUT,POST,DELETE,OPTIONS,PATCH");
		responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
		//responseContext.getHeaders().add("Access-Control-Allow-Origin", "https://universe-fct.oa.r.appspot.com");
		responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, Authorization, X-FCM-Token");
	}
}