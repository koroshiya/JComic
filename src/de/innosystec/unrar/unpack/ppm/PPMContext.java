/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 31.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * the unrar licence applies to all junrar source and binary distributions 
 * you are not allowed to use this source to re-create the RAR compression algorithm
 * 
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package de.innosystec.unrar.unpack.ppm;

import de.innosystec.unrar.io.Raw;

/**
 * DOCUMENT ME
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class PPMContext extends Pointer
{

    private static final int unionSize = Math.max(FreqData.size, State.size);

	public static final int size = 2 + unionSize + 4; // 12

    // ushort NumStats;
	private int numStats; // determines if feqData or onstate is used

	// (1==onestate)

	private final FreqData freqData; // -\

	// |-> union
	private final State oneState; // -/

	private int suffix; // pointer ppmcontext

	public final static int[] ExpEscape =
            { 25, 14, 9, 7, 5, 5, 4, 4, 4, 3, 3, 3, 2, 2, 2, 2 };

    // Temp fields
    private final int[] ps = new int[256];

	public PPMContext(byte[] mem)
	{
		super(mem);
		oneState = new State(mem);
		freqData = new FreqData(mem);
	}

	public FreqData getFreqData()
	{
		return freqData;
	}

	public final int getNumStats()
	{
		if (mem!=null){
			numStats = Raw.readShortLittleEndian(mem,  pos)&0xffff;
		}
		return numStats;
	}

	public final void setNumStats(int numStats)
	{
		this.numStats = numStats&0xffff;
		if (mem != null) {
			Raw.writeShortLittleEndian(mem, pos, (short)numStats);
		}
	}

	public State getOneState()
	{
		return oneState;
	}

	public void setOneState(StateRef oneState)
	{
		this.oneState.setValues(oneState);
	}

	public int getSuffix()
	{
		if(mem!=null){
			suffix = Raw.readIntLittleEndian(mem,  pos+8);
		}
		return suffix;
	}

	public void setSuffix(PPMContext suffix)
	{
		setSuffix(suffix.getAddress());
	}

	public void setSuffix(int suffix)
	{
		this.suffix = suffix;
		if (mem != null) {
			Raw.writeIntLittleEndian(mem, pos + 8, suffix);
		}
	}

	@Override
	public void setAddress(int pos)
	{
        super.setAddress(pos);
        oneState.setAddress(pos+2);
        freqData.setAddress(pos+2);
	}

    private PPMContext getTempPPMContext(byte[] mem) {
        return new PPMContext(mem);
    }

	public int createChild(ModelPPM model, State pStats/* ptr */, StateRef firstState /* ref */){
		PPMContext pc = getTempPPMContext(model.getSubAlloc().getHeap());
		pc.setAddress(model.getSubAlloc().allocContext());
        pc.setNumStats(1);
        pc.setOneState(firstState);
        pc.setSuffix(this);
        pStats.setSuccessor(pc);
		return pc.getAddress();
	}

	public void rescale(ModelPPM model)
	{
		int OldNS = getNumStats(), i = getNumStats() - 1, Adder, EscFreq;
		// STATE* p1, * p;
		State p1 = new State(model.getHeap());
		State p = new State(model.getHeap());
		State temp = new State(model.getHeap());

		for (p.setAddress(model.getFoundState().getAddress()); p.getAddress() != freqData.getStats(); p.decAddress()) {
			temp.setAddress(p.getAddress() - State.size);
			State.ppmdSwap(p, temp);
		}
		temp.setAddress(freqData.getStats());
		temp.incFreq(4);
		freqData.incSummFreq(4);
		EscFreq = freqData.getSummFreq() - p.getFreq();
		Adder = (model.getOrderFall() != 0) ? 1 : 0;
		p.setFreq((p.getFreq() + Adder) >>> 1);
		freqData.setSummFreq(p.getFreq());
		do {
            p.incAddress();
			EscFreq -= p.getFreq();
			p.setFreq((p.getFreq() + Adder) >>> 1);
			freqData.incSummFreq(p.getFreq());
			temp.setAddress(p.getAddress() - State.size);
			if (p.getFreq() > temp.getFreq()) {
				p1.setAddress(p.getAddress());
                StateRef tmp = new StateRef();
                tmp.setValues(p1);
				State temp2 = new State(model.getHeap());
				State temp3 = new State(model.getHeap());
				do {
					temp2.setAddress(p1.getAddress() - State.size);
					p1.setValues(temp2);
                    p1.decAddress();
					temp3.setAddress(p1.getAddress() - State.size);
				} while (p1.getAddress() != freqData.getStats() && tmp.getFreq() > temp3.getFreq());
				p1.setValues(tmp);
			}
		} while (--i != 0);
		if (p.getFreq() == 0) {
			do {
				i++;
				p.decAddress();
			} while (p.getFreq() == 0);
			EscFreq += i;
            setNumStats(getNumStats() - i);
			if (getNumStats() == 1) {
				StateRef tmp = new StateRef();
				temp.setAddress(freqData.getStats());
				tmp.setValues(temp);
				do {
					tmp.decFreq(tmp.getFreq() >>> 1);
					EscFreq >>>= 1;
				} while (EscFreq > 1);
				model.getSubAlloc().freeUnits(freqData.getStats(),(OldNS + 1) >>> 1);
				oneState.setValues(tmp);
				model.getFoundState().setAddress(oneState.getAddress());
				return;
			}
		}
		EscFreq -= EscFreq >>> 1;
		freqData.incSummFreq(EscFreq);
		int n0 = (OldNS + 1) >>> 1, n1 = (getNumStats() + 1) >>> 1;
		if (n0 != n1) {
			freqData.setStats(model.getSubAlloc().shrinkUnits(freqData.getStats(), n0, n1));
		}
		model.getFoundState().setAddress(freqData.getStats());
	}

	private int getArrayIndex(ModelPPM Model, State rs)
	{
		PPMContext tempSuffix = getTempPPMContext(Model.getSubAlloc().getHeap());
		tempSuffix.setAddress(getSuffix());
		int ret = 0;
		ret += Model.getPrevSuccess();
		ret += Model.getNS2BSIndx()[tempSuffix.getNumStats() - 1];
		ret += Model.getHiBitsFlag() + 2* Model.getHB2Flag()[rs.getSymbol()];
		ret += ((Model.getRunLength() >>> 26) & 0x20);
		return ret;
	}

    public int getMean(int summ){
		return ( (summ + (1 << (ModelPPM.PERIOD_BITS - 2) ) ) >>> (ModelPPM.PERIOD_BITS) );
	}

	public void decodeBinSymbol(ModelPPM model)
	{
		State rs = new State(model.getHeap());
		rs.setAddress(oneState.getAddress());// State&
		model.setHiBitsFlag(model.getHB2Flag()[model.getFoundState().getSymbol()]);
		int off1 = rs.getFreq() - 1;
		int off2 = getArrayIndex(model, rs);
		int bs = model.getBinSumm()[off1][off2];
		if (model.getCoder().getCurrentShiftCount() < bs) {
			model.getFoundState().setAddress(rs.getAddress());
			rs.incFreq((rs.getFreq() < 128) ? 1 : 0);
			model.getCoder().getSubRange().setLowCount(0);
			model.getCoder().getSubRange().setHighCount(bs); 
			bs = ((bs + ModelPPM.INTERVAL - getMean(bs)) & 0xffff);
			model.getBinSumm()[off1][off2] = bs;
			model.setPrevSuccess(1); 
			model.incRunLength(1);
		} else {
			model.getCoder().getSubRange().setLowCount(bs);
			bs = (bs - getMean(bs)) & 0xFFFF;
			model.getBinSumm()[off1][off2] = bs;
			model.getCoder().getSubRange().setHighCount(ModelPPM.BIN_SCALE);
			model.setInitEsc(ExpEscape[bs >>> 10]);
			model.setNumMasked(1);
			model.getCharMask()[rs.getSymbol()] = model.getEscCount();
			model.setPrevSuccess(0);
			model.getFoundState().setAddress(0);
		}
	}

	public void update1(ModelPPM model, int p/* ptr */)
	{
		model.getFoundState().setAddress(p);
		model.getFoundState().incFreq(4);
		freqData.incSummFreq(4);
		State p0 = new State(model.getHeap());
		State p1 = new State(model.getHeap());
		p0.setAddress(p);
		p1.setAddress(p - State.size);
		if (p0.getFreq() > p1.getFreq()) {
			State.ppmdSwap(p0, p1);
			model.getFoundState().setAddress(p1.getAddress());
			if (p1.getFreq() > ModelPPM.MAX_FREQ)
				rescale(model);
		}
	}

	public boolean decodeSymbol2(ModelPPM model)
	{
		long count;
        int hiCnt, i = getNumStats() - model.getNumMasked();
		SEE2Context psee2c = makeEscFreq2(model, i);
		RangeCoder coder = model.getCoder();
		// STATE* ps[256], ** pps=ps, * p=U.Stats-1;
		State p = new State(model.getHeap());
		State temp = new State(model.getHeap());
		p.setAddress(freqData.getStats() - State.size); 
		int pps = 0;
		hiCnt = 0;

		do {
			do {
				p.incAddress();// p++;
			} while (model.getCharMask()[p.getSymbol()] == model.getEscCount());
			hiCnt += p.getFreq();
			ps[pps++] = p.getAddress();
		} while (--i != 0);
		coder.getSubRange().incScale(hiCnt);
		count = coder.getCurrentCount();
		if (count >= coder.getSubRange().getScale()) {
			return false;
		}
		pps = 0;
		p.setAddress(ps[pps]);
		if (count < hiCnt) {
			hiCnt = 0;
			while ((hiCnt += p.getFreq()) <= count) {
				p.setAddress(ps[++pps]);// p=*++pps;
			}
			coder.getSubRange().setHighCount(hiCnt);
			coder.getSubRange().setLowCount(hiCnt - p.getFreq());
			psee2c.update();
			update2(model, p.getAddress());
		} else {
			coder.getSubRange().setLowCount(hiCnt);
			coder.getSubRange().setHighCount(coder.getSubRange().getScale());
			i = getNumStats() - model.getNumMasked();// ->NumMasked;
			pps--;
			do {
				temp.setAddress(ps[++pps]);// (*++pps)
				model.getCharMask()[temp.getSymbol()] = model.getEscCount();
			} while (--i != 0);
			psee2c.incSumm((int)coder.getSubRange().getScale());
			model.setNumMasked(getNumStats());
		}
		return (true);
	}

	public void update2(ModelPPM model, int p)
	{
		State temp = new State(model.getHeap());
		temp.setAddress(p);
		model.getFoundState().setAddress(p);
		model.getFoundState().incFreq(4); 
		freqData.incSummFreq(4);
		if (temp.getFreq() > ModelPPM.MAX_FREQ) {
			rescale(model);
		}
		model.incEscCount();
		model.setRunLength(model.getInitRL());
	}

	private SEE2Context makeEscFreq2(ModelPPM model, int Diff)
	{
		SEE2Context psee2c;
        int numStats = getNumStats();
		if (numStats != 256) {
			PPMContext suff = getTempPPMContext(model.getHeap());
			suff.setAddress(getSuffix());
            int idx1 = model.getNS2Indx()[Diff - 1];
            int idx2 = 0;
            idx2 += (Diff < suff.getNumStats() - numStats) ? 1 : 0;
			idx2 += 2 * ((freqData.getSummFreq() < 11 * numStats) ? 1 : 0); 
			idx2 += 4 * ((model.getNumMasked() > Diff) ? 1 : 0); 
			idx2 += model.getHiBitsFlag();
			psee2c = model.getSEE2Cont()[idx1][idx2];
			model.getCoder().getSubRange().setScale(psee2c.getMean());
		} else {
			psee2c = model.getDummySEE2Cont();
			model.getCoder().getSubRange().setScale(1);
		}
		return psee2c;
	}

	public boolean decodeSymbol1(ModelPPM model)
	{

		RangeCoder coder = model.getCoder();
		coder.getSubRange().setScale(freqData.getSummFreq());
		State p = new State(model.getHeap());
		p.setAddress(freqData.getStats());
		int i, HiCnt;
		long count = coder.getCurrentCount();
		if (count >= coder.getSubRange().getScale()) {
			return false;
		}
		if (count < (HiCnt = p.getFreq())) {
			coder.getSubRange().setHighCount(HiCnt);
			model.setPrevSuccess((2 * HiCnt > coder.getSubRange().getScale()) ? 1 : 0);
			model.incRunLength(model.getPrevSuccess());
			HiCnt += 4;
			model.getFoundState().setAddress(p.getAddress());
			model.getFoundState().setFreq(HiCnt);
			freqData.incSummFreq(4);
			if (HiCnt > ModelPPM.MAX_FREQ) {
				rescale(model);
			}
			coder.getSubRange().setLowCount(0);
			return true;
		} else {
			if (model.getFoundState().getAddress() == 0) {
				return (false);
			}
		}
		model.setPrevSuccess(0);
        int numStats = getNumStats();
		i = numStats - 1;
		while ((HiCnt += p.incAddress().getFreq()) <= count)
		{
			if (--i == 0) {
				model.setHiBitsFlag(model.getHB2Flag()[model.getFoundState().getSymbol()]);
				coder.getSubRange().setLowCount(HiCnt);
				model.getCharMask()[p.getSymbol()] = model.getEscCount();
				model.setNumMasked(numStats);
				i = numStats - 1;
				model.getFoundState().setAddress(0);
				do {
					model.getCharMask()[p.decAddress().getSymbol()] = model.getEscCount();
				} while (--i != 0);
				coder.getSubRange().setHighCount(coder.getSubRange().getScale());
				return (true);
			}
		}
		coder.getSubRange().setLowCount(HiCnt-p.getFreq());
		coder.getSubRange().setHighCount(HiCnt);
		update1(model, p.getAddress());
		return (true);
	}

    public String toString() {
        return "PPMContext["+
        "\n  pos="+
        pos+
        "\n  size="+
        size+
        "\n  numStats="+
        getNumStats()+
        "\n  Suffix="+
        getSuffix()+
        "\n  freqData="+
        freqData+
        "\n  oneState="+
        oneState+
        "\n]";
    }

}
