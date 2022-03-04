package uk.ac.man.cs.eventlite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

	public static final String ADMIN_ROLE = "ADMINISTRATOR";
	public static final String ORGANIZER_ROLE = "ORGANIZER";

	// List the mappings/methods for which no authorisation is required.
	// By default we allow all GETs and full access to the H2 console.
//	private static final RequestMatcher[] NO_AUTH = { new AntPathRequestMatcher("/webjars/**", "GET"),
//			new AntPathRequestMatcher("/events"), new AntPathRequestMatcher("/events/{id:[\\d]+}", "GET"),
//			new AntPathRequestMatcher("/h2-console/**"), new AntPathRequestMatcher("/events/search/**", "GET")};
//
//	private static final RequestMatcher[] ORGANIZER_AUTH = { new AntPathRequestMatcher("/events", "POST"), new AntPathRequestMatcher("/events/new"),
//			new AntPathRequestMatcher("/events/{id:[\\d]+}", "POST"), new AntPathRequestMatcher("/events/update/**")};

	private static final RequestMatcher[] NO_AUTH = { new AntPathRequestMatcher("/webjars/**", "GET"),
			new AntPathRequestMatcher("/**", "GET"), new AntPathRequestMatcher("/h2-console/**"),
			new AntPathRequestMatcher("/events/search", "GET")};

	private static final RequestMatcher[] ORGANIZER_AUTH = { new AntPathRequestMatcher("/events", "POST"),
			new AntPathRequestMatcher("/events/{id:[\\d]+}"), new AntPathRequestMatcher("/events/update/{id:[\\d]+}"), 
			new AntPathRequestMatcher("/events/new")};

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// By default, all requests are authenticated except our specific list.
		http.authorizeRequests().requestMatchers(NO_AUTH).permitAll();
		http.authorizeRequests().requestMatchers(ORGANIZER_AUTH).hasRole(ORGANIZER_ROLE);
		http.authorizeRequests().anyRequest().hasRole(ADMIN_ROLE);

		// Use form login/logout for the Web.
		http.formLogin().loginPage("/sign-in").permitAll();
		http.logout().logoutUrl("/sign-out").logoutSuccessUrl("/").permitAll();

		// Use HTTP basic for the API.
		http.requestMatcher(new AntPathRequestMatcher("/api/**")).httpBasic();

		// Only use CSRF for Web requests.
		// Disable CSRF for the API and H2 console.
		http.antMatcher("/**").csrf().ignoringAntMatchers("/api/**", "/h2-console/**");

		// Disable X-Frame-Options for the H2 console.
		http.headers().frameOptions().disable();
	}

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		UserDetails rob = User.withUsername("Rob").password(encoder.encode("Haines")).roles(ADMIN_ROLE).build();
		UserDetails caroline = User.withUsername("Caroline").password(encoder.encode("Jay")).roles(ADMIN_ROLE).build();
		UserDetails markel = User.withUsername("Markel").password(encoder.encode("Vigo")).roles(ADMIN_ROLE).build();
		UserDetails mustafa = User.withUsername("Mustafa").password(encoder.encode("Mustafa")).roles(ADMIN_ROLE)
				.build();
		UserDetails tom = User.withUsername("Tom").password(encoder.encode("Carroll")).roles(ORGANIZER_ROLE).build();

		return new InMemoryUserDetailsManager(rob, caroline, markel, mustafa, tom);
	}
}
