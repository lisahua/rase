/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package p1;

import java.io.ObjectStreamException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.util.StringUtils;

/**
 * Superclass for AOP proxy configuration managers. These are not themselves AOP
 * proxies, but subclasses of this class are normally factories from which AOP
 * proxy instances are obtained directly.
 * 
 * <p>
 * This class frees subclasses of the housekeeping of Advices and Advisors, but
 * doesn't actually implement proxy creation methods, which are provided by
 * subclasses.
 * 
 * <p>
 * This class is serializable; subclasses need not be. This class is used to
 * hold snapshots of proxies.
 * 
 * @author Rod Johnson
 * @version $Id: AdvisedSupport.java,v 1.36 2004-07-27 15:52:25 johnsonr Exp $
 * @see org.springframework.aop.framework.AopProxy
 */
public class AdvisedSupport extends ProxyConfig implements Advised {

	/**
	 * Canonical TargetSource when there's no target, and behavior is supplied
	 * by the advisors.
	 */
	public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;

	/** List of AdvisedSupportListener */
	private transient List listeners = new LinkedList();

	/**
	 * Package-protected to allow direct access for efficiency
	 */
	TargetSource targetSource = EMPTY_TARGET_SOURCE;

	transient AdvisorChainFactory advisorChainFactory;

	/**
	 * List of Advice. If an Interceptor is added, it will be wrapped in an
	 * Advice before being added to this List.
	 */
	private List advisors = new LinkedList();

	/**
	 * Array updated on changes to the advisors list, which is easier to
	 * manipulate internally
	 */
	private Advisor[] advisorArray = new Advisor[0];

	/** Interfaces to be implemented by the proxy */
	private Set interfaces = new HashSet();

	/**
	 * Set to true when the first AOP proxy has been created, meaning that we
	 * must track advice changes via onAdviceChange() callback.
	 */
	private transient boolean isActive;

	/**
	 * No arg constructor to allow use as a JavaBean.
	 */
	public AdvisedSupport() {
		initDefaultAdvisorChainFactory();
	}

	/**
	 * Create a DefaultProxyConfig with the given parameters.
	 * 
	 * @param interfaces
	 *            the proxied interfaces
	 */
	public AdvisedSupport(Class[] interfaces) {
		this();
		setInterfaces(interfaces);
	}

	/**
	 * Used to initialize transient state
	 */
	protected Object readResolve() throws ObjectStreamException {
		// Initialize transient fields
		AdvisedSupport copy = this;

		// If we're in a non-serializable subclass,
		// copy into an AdvisedSupport object.
		if (getClass() != AdvisedSupport.class) {
			copy = new AdvisedSupport();
			copy.copyConfigurationFrom(this);
		}

		this.logger = LogFactory.getLog(getClass());
		this.isActive = true;
		this.listeners = new LinkedList();
		initDefaultAdvisorChainFactory();
		return this;
	}

}
