package org.baldurproject.boards;

import org.baldurproject.Clock;
import org.baldurproject.Input;
import org.baldurproject.Module;
import org.baldurproject.Output;
import org.baldurproject.Net;

@Module
public class Xula2Board { 
	@Clock  @Input public final Net<Boolean> FPGA_CLK = new Net<Boolean>(false);

	@Output public final Net<Boolean> LD0 = new Net<Boolean>(false);
	
	
	@Input  public final Net<Boolean> CLK2 = new Net<Boolean>(false); 
	
	@Output public final Net<Boolean> LD7 = new Net<Boolean>(false);
	@Output public final Net<Boolean> LD6 = new Net<Boolean>(false);
	@Output public final Net<Boolean> LD5 = new Net<Boolean>(false);
	@Output public final Net<Boolean> LD4 = new Net<Boolean>(false);
	@Output public final Net<Boolean> LD3 = new Net<Boolean>(false);
	@Output public final Net<Boolean> LD2 = new Net<Boolean>(false);
	@Output public final Net<Boolean> LD1 = new Net<Boolean>(false);

	@Input  public final Net<Boolean> SW7 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> SW6 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> SW5 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> SW4 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> SW3 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> SW2 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> SW1 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> SW0 = new Net<Boolean>(false);
	
	@Input  public final Net<Boolean> BTN3 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> BTN2 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> BTN1 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> BTN0 = new Net<Boolean>(false);

	@Input  public final Net<Boolean> JA1 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> JA2 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JA3 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JA4 = new Net<Boolean>(false);

	@Input  public final Net<Boolean> JB1 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> JB2 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JB3 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JB4 = new Net<Boolean>(false);
	
	@Input  public final Net<Boolean> JC1 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> JC2 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JC3 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JC4 = new Net<Boolean>(false);

	@Input  public final Net<Boolean> JD1 = new Net<Boolean>(false);
	@Input  public final Net<Boolean> JD2 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JD3 = new Net<Boolean>(false);
	@Output public final Net<Boolean> JD4 = new Net<Boolean>(false);
	
	@Output public final Net<Boolean> RED0 = new Net<Boolean>(false);
	@Output public final Net<Boolean> RED1 = new Net<Boolean>(false);
	@Output public final Net<Boolean> BLU0 = new Net<Boolean>(false);
	@Output public final Net<Boolean> BLU1 = new Net<Boolean>(false);
	@Output public final Net<Boolean> GRN0 = new Net<Boolean>(false);
	@Output public final Net<Boolean> GRN1 = new Net<Boolean>(false);
	@Output public final Net<Boolean> GRN2 = new Net<Boolean>(false);
	@Output public final Net<Boolean> HS = new Net<Boolean>(false);
	@Output public final Net<Boolean> VS = new Net<Boolean>(false);
	
	@Output public final Net<Boolean> AN3 = new Net<Boolean>(false);
	@Output public final Net<Boolean> AN2 = new Net<Boolean>(false);
	@Output public final Net<Boolean> AN1 = new Net<Boolean>(false);
	@Output public final Net<Boolean> AN0 = new Net<Boolean>(false);
	
	@Output public final Net<Boolean> SEGA = new Net<Boolean>(false);
	@Output public final Net<Boolean> SEGB = new Net<Boolean>(false);
	@Output public final Net<Boolean> SEGC = new Net<Boolean>(false);
	@Output public final Net<Boolean> SEGD = new Net<Boolean>(false);
	@Output public final Net<Boolean> SEGE = new Net<Boolean>(false);
	@Output public final Net<Boolean> SEGF = new Net<Boolean>(false);
	@Output public final Net<Boolean> SEGG = new Net<Boolean>(false);
	
}
