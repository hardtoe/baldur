package org.baldurproject.simulate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.TreeMap;

import org.baldurproject.Always;
import org.baldurproject.Net;


public class Scheduler {
	private long currentTime;
	private TreeMap<Long, LinkedList<Runnable>> queue;
	
	private static final Scheduler singleton =
		new Scheduler();
	
	public static Scheduler get() {
		return singleton;
	}
	
	private Scheduler() {
		currentTime = 0;
		queue = new TreeMap<Long, LinkedList<Runnable>>();
	}
	
	public void run() {
		run(Long.MAX_VALUE);
	}
	
	public void run(long duration) {
		long startTime =
			currentTime;
		
		while (
			currentTime - startTime <= duration &&
			!queue.isEmpty()
		) {
			currentTime = queue.firstKey();
			
			LinkedList<Runnable> currentDelta =
				queue.remove(currentTime);
			
			while (!currentDelta.isEmpty()) {
				currentDelta.removeFirst().run();
			}
		}
	}
	
	public void schedule(long delay, Runnable r) {
		long scheduledTime = 
			currentTime + delay;
		
		LinkedList<Runnable> scheduledDelta =
			queue.get(scheduledTime);
		
		if (scheduledDelta == null) {
			scheduledDelta =
				new LinkedList<Runnable>();
			
			queue.put(scheduledTime, scheduledDelta);
		}
		
		scheduledDelta.add(r);
	}
	

	
	public void initialize(final Object o) {
		for (final Method m : o.getClass().getMethods()) {
			Annotation a = getAlwaysBlockAnnotation(m);
			
			if (a != null) {
				Always alwaysAnnotation = 
					(Always) m.getAnnotation(a.annotationType());
				
				Runnable block = new Runnable() {
					public void run() {
						try {
							m.invoke(o);
							
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							
						} catch (SecurityException e) {
							e.printStackTrace();
							
						} catch (IllegalAccessException e) {
							e.printStackTrace();
							
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				};
				
				for (String netName : alwaysAnnotation.value()) {
					try {
						Net n = (Net) o.getClass().getField(netName).get(this);
						n.addCallback(block);
						
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						
					} catch (SecurityException e) {
						e.printStackTrace();
						
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private Annotation getAlwaysBlockAnnotation(final Method m) {
		for (Annotation a : m.getAnnotations()) {
			if (a.annotationType() == Always.class) {
				return a;
			}
		}
		
		return null;
	}
}