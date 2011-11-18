package dojo;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FilterTest {

	@Mock
	private HttpServletResponse response;
	private AuthenticationFilter testee;
	@Mock
	private HttpServletRequest request;
	@Mock
	private FilterChain filterChain;
	@Mock
	private HttpSession httpSession;

	@Before
	public void setUp() {
		when(request.getSession()).thenReturn(httpSession);
	}
	
	@Test
	public void withAnEmptyRequestStatus401IsSetOnTheResponse() throws Exception {
		testee = new AuthenticationFilter();

		testee.doFilter(request, response, filterChain);

		verifyNotLoggedIn();
	}

	@Test
	public void requestContainsInvalidLoginCredentials() throws Exception {
		testee = new AuthenticationFilter();
		when(request.getParameter("username")).thenReturn("invalidUsername");
		when(request.getParameter("password")).thenReturn("invalidPassword");

		testee.doFilter(request, response, filterChain);

		verifyNotLoggedIn();
	}

	@Test
	public void requestContainsValidLoginCredentials() throws Exception {
		testee = new AuthenticationFilter();
		when(request.getParameter("username")).thenReturn("validUsername");
		when(request.getParameter("password")).thenReturn("validPassword");

		testee.doFilter(request, response, filterChain);

		verify(response).addCookie(argThat(Matchers.<Cookie>hasProperty("name", equalTo("SSOToken"))));
		verify(httpSession).setAttribute(eq("SSOToken"), anyString());
		verifyLoggedIn();
	}

	private void verifyNotLoggedIn() {
		verifyZeroInteractions(filterChain);
		verify(response).setStatus(401);
	}
	
	private void verifyLoggedIn() throws Exception {
		verify(filterChain).doFilter(request, response);
		verify(response, never()).setStatus(401);
	}
}
