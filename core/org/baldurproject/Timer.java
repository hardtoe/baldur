package org.baldurproject;

@Module
public class Timer {
	@Local private int delay;
	@Local private int milliDelay;
	
	@Blocking
	public void delayClocks(int length) {
		while (delay < (length)) { 
			delay++;
		}
		
		delay = 0;
	}
	
	@Blocking
	public void delayMillis(int length) {
		while (delay < length) {
			delay++;
			millisecond();
		}
		
		delay = 0;
	}

	@Blocking
	public void millisecond() {
		while (milliDelay < 100000) {
			milliDelay++;
		}
		
		milliDelay = 0;
	}
	
	@Blocking
	public void clock() {
		// wait one clock
	}
}
