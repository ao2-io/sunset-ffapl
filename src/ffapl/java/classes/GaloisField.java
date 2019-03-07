/**
 *
 */
package ffapl.java.classes;

import ffapl.exception.FFaplException;
import ffapl.java.exception.FFaplAlgebraicException;
import ffapl.java.interfaces.IAlgebraicError;
import ffapl.java.interfaces.IAlgebraicOperations;
import ffapl.java.interfaces.IJavaType;
import ffapl.java.math.Algorithm;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * @author Alexander Ortner
 * @version 1.0
 *
 */
public class GaloisField implements IJavaType<GaloisField>, Comparable<GaloisField>, IAlgebraicOperations<GaloisField>{

	private Prime _p;
	private PolynomialRC _irrply;
	private PolynomialRC _value;
	/** the thread within the Galois Field is created */
	private Thread _thread;


	/**
	 *
	 * @param characteristic
	 * @param polynomial
	 * @param thread
	 * @throws FFaplAlgebraicException
	 */
	public GaloisField(BigInteger characteristic, Polynomial polynomial, Thread thread) throws FFaplAlgebraicException{
		_thread = thread;
		_p = new Prime(characteristic, _thread);
		_irrply = new PolynomialRCPrime(polynomial.polynomial(), _p, _thread);

		if(! Algorithm.isIrreducible(_irrply)){
			Object[] arguments = {_irrply};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.NOTIRREDUCIBLE);
		}
		_value = new PolynomialRCPrime(_p, _thread);
	}

	/**
	 *
	 * @param characteristic
	 * @param polynomial
	 * @throws FFaplAlgebraicException
	 */
	public GaloisField(long characteristic, Polynomial polynomial, Thread thread) throws FFaplAlgebraicException{
		this(BigInteger.valueOf(characteristic), polynomial, thread);
	}


	/**
	 * Return a copy of the irreducible polynomial
	 * @return
	 */
	public PolynomialRCPrime irrPolynomial(){
		return (PolynomialRCPrime) this._irrply.clone();
	}

	/**
	 * Return a copy of the value
	 * @return
	 */
	public PolynomialRCPrime value(){
		return (PolynomialRCPrime) _value.clone();
	}

	/**
	 * Sets the polynomial value of the GF
	 * @param ply
	 * @throws FFaplAlgebraicException
	 */
	public void setValue(Polynomial ply) throws FFaplAlgebraicException{
		if (!characteristicMatches(ply))
			return;

		_value.setPolynomial(ply.polynomial());
		_value.mod(_irrply);
	}

	/**
	 * Returns the characteristic of the GF
	 * @return
	 */
	public BigInteger characteristic(){
		return this._p;
	}


	/**
	 * Adds the value of the GaloisField <Code> gf </Code>
	 * @param gf
	 * @throws FFaplAlgebraicException
	 *         <GF_NOTCOMPATIBLE> if GaloisField's are incompatible
	 */
	public void add(GaloisField gf) throws FFaplAlgebraicException{

		if(! this.equalGF(gf)){
			Object[] arguments ={this.classInfo(), gf.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.TYPES_INCOMPATIBLE);
		}

		_value.add(gf.value());
		_value.mod(_irrply);
	}

	/**
	 * Adds the ply
	 * @param ply
	 * @throws FFaplAlgebraicException
	 */
	public void add(Polynomial ply) throws FFaplAlgebraicException{
		if (!characteristicMatches(ply))
			return;

		_value.add(ply);
		_value.mod(_irrply);
	}

	/**
	 * adds the summand
	 * @param summand
	 * @throws FFaplAlgebraicException
	 */
	public void add(BigInteger summand) throws FFaplAlgebraicException{
		_value.add(summand, ZERO);
		_value.mod(_irrply);
	}

	/**
	 * Add
	 * @param rc
	 * @throws FFaplAlgebraicException
	 */
	public void add(ResidueClass rc) throws FFaplAlgebraicException {
		if(!rc.modulus().equals(_p)){
			Object[] arguments ={this.characteristic(), this.classInfo(),
		            rc.modulus(), rc.classInfo()};
					throw new FFaplAlgebraicException(arguments, IAlgebraicError.CHARACTERISTIC_UNEQUAL);
		}
		add(rc.value());
	}

	/**
	 * Subtracts the value of the GaloisField <Code> gf </Code>
	 * @param gf
	 * @throws FFaplAlgebraicException
	 *         <GF_NOTCOMPATIBLE> if GaloisField's are incompatible
	 */
	public void subtract(GaloisField gf) throws FFaplAlgebraicException{
		if(! this.equalGF(gf)){
			Object[] arguments ={this.classInfo(), gf.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.TYPES_INCOMPATIBLE);
		}

		_value.subtract(gf.value());
		_value.mod(_irrply);
	}


	/**
	 * Subtracts the polynomial
	 * @param ply
	 * @throws FFaplAlgebraicException
	 */
	public void subtract(Polynomial ply) throws FFaplAlgebraicException{
		if (!characteristicMatches(ply))
			return;

		_value.subtract(ply);
		_value.mod(_irrply);
	}

	/**
	 * Subtracts the subtrahend
	 * @param subtrahend
	 * @throws FFaplAlgebraicException
	 */
	public void subtract(BigInteger subtrahend) throws FFaplAlgebraicException{
		_value.subtract(subtrahend, ZERO);
		_value.mod(_irrply);
	}

	/**
	 * Subtracts the subtrahend
	 * @param rc
	 * @throws FFaplAlgebraicException
	 */
	public void subtract(ResidueClass rc) throws FFaplAlgebraicException {
		if(! _p.equals(rc.modulus())){
			Object[] arguments ={this.characteristic(), this.classInfo(),
		            rc.modulus(), rc.classInfo()};
					throw new FFaplAlgebraicException(arguments, IAlgebraicError.CHARACTERISTIC_UNEQUAL);
		}
		subtract(rc.value());
	}



	/**
	 * Multiplies the value of the GaloisField <Code> gf </Code>
	 * @param gf
	 * @throws FFaplAlgebraicException
	 *         <GF_NOTCOMPATIBLE> if GaloisField's are incompatible
	 */
	public void multiply(GaloisField gf) throws FFaplAlgebraicException{
		if(! this.equalGF(gf)){
			Object[] arguments ={this.classInfo(), gf.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.TYPES_INCOMPATIBLE);
		}

		_value.multiply(gf.value());
		_value.mod(_irrply);
	}

	/**
	 * Multiplies the value of the Polynomial <Code> ply </Code>
	 * @param ply
	 * @throws FFaplAlgebraicException
	 */
	public void multiply(Polynomial ply) throws FFaplAlgebraicException{
		if (!characteristicMatches(ply))
			return;

		_value.multiply(ply);
		_value.mod(_irrply);
	}

	/**
	 * Multiplies the value of the factor <Code> factor </Code>
	 * @param factor
	 * @throws FFaplAlgebraicException
	 */
	public void multiply(BigInteger factor) throws FFaplAlgebraicException{
		_value.multiply(factor, ZERO);
		_value.mod(_irrply);
	}

	/**
	 * Multiplies the value of the residue class <Code> rc </Code>
	 * @param rc
	 * @throws FFaplAlgebraicException
	 */
	public void multiply(ResidueClass rc) throws FFaplAlgebraicException {
		if(!rc.modulus().equals(this._p)){
			//characteristic and modulus unequal
			Object[] arguments ={this.characteristic(), this.classInfo(),
		            rc.modulus(), rc.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.CHARACTERISTIC_UNEQUAL);
		}
		this.multiply(rc.value());
	}

    /**
     * multiplies the multiplicative inverse of <Code> gf </Code> to the value of the GF.
     * @param gf
     * @throws FFaplAlgebraicException
     */
    public void divide(GaloisField gf) throws FFaplAlgebraicException{
    	if(! this.equalGF(gf)){
    		Object[] arguments ={this.classInfo(), gf.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.TYPES_INCOMPATIBLE);
		}
    	gf = GaloisField.inverse(gf);
    	this.multiply(gf);
    }

    /**
     * multiplies the multiplicative inverse of <Code> ply </Code> to the value of the GF.
     * @param ply
     * @throws FFaplAlgebraicException
     */
    public void divide(Polynomial ply) throws FFaplAlgebraicException{
    	GaloisField gf;
    	if(ply instanceof PolynomialRCPrime ){
    		Object[] arguments ={"Error in GF divide"};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.INTERNAL);
		}

    	gf = this.clone();
    	gf.setValue(ply);
    	gf = GaloisField.inverse(gf);
    	this.multiply(gf);
    }

    /**
     * multiplies the multiplicative inverse of <Code> divisor </Code> to the value of the GF.
     * @param divisor
     * @throws FFaplAlgebraicException
     */
    public void divide(BigInteger divisor) throws FFaplAlgebraicException{
    	divide(new Polynomial(divisor, ZERO, _thread));
    }

    /**
     * multiplies the multiplicative inverse of <Code> rc </Code> to the value of the GF.
     * @param rc
     * @throws FFaplAlgebraicException
     */
    public void divide(ResidueClass rc) throws FFaplAlgebraicException{
    	if(!rc.modulus().equals(_p)){
    		Object[] arguments ={this.characteristic(), this.classInfo(),
		               			rc.modulus(), rc.classInfo()};
    		throw new FFaplAlgebraicException(arguments, IAlgebraicError.CHARACTERISTIC_UNEQUAL);
    	}
    	divide(rc.value());
    }

    /**
	 * calculates polynomial^exponent
	 * @param exponent
	 * @throws FFaplAlgebraicException
	 */
	public void pow(BigInteger exponent) throws FFaplAlgebraicException{
		//
		//BigInteger exp = exponent.abs().mod(_p.subtract(BigInteger.ONE));
		BigInteger exp = exponent.abs();
		/*if(_value.degree().equals(BigInteger.ZERO)){
			//a^p-1 = 1 mod p  -> exponent mod p-1
			exp = exponent.abs().mod(_p.subtract(BigInteger.ONE));
		}*/

		this.setValue(Algorithm.squareAndMultiply(this._value, exp, this._irrply));
		if(exponent.compareTo(ZERO) < 0){
			this.setValue(GaloisField.inverse(this).value());
		}
	}

	/**
	 * calculates polynomial^exponent
	 * @param exponent
	 * @throws FFaplAlgebraicException
	 */
	public void pow(long exponent) throws FFaplAlgebraicException{
		pow(BigInteger.valueOf(exponent));
	}

	/**
	 * Returns true if GaloisFields are equal
	 * @return true if characteristic and irreducible polynomial are equal, false otherwise
	 * @throws FFaplAlgebraicException
	 */
	public boolean equalGF(GaloisField gf) throws FFaplAlgebraicException{
		return this._irrply.equals(gf.irrPolynomial());
	}

	/**
	 * Equals
	 * @param val
	 * @return
	 * @throws FFaplAlgebraicException
	 */
	public boolean equals(IJavaType val) throws FFaplAlgebraicException{
		GaloisField gf;
		if(val instanceof GaloisField){
			gf = (GaloisField) val;
			if(! this.equalGF(gf)){
				Object[] arguments ={this.classInfo(), gf.classInfo()};
				throw new FFaplAlgebraicException(arguments, IAlgebraicError.TYPES_INCOMPATIBLE);
			}
		}else if(val instanceof BigInteger){
			gf = this.clone();
			gf.setValue(new Polynomial((BigInteger) val, ZERO, _thread));
		}else if(val instanceof PolynomialRC){

			if(! this.characteristic().equals(((PolynomialRC)val).characteristic())){
				Object[] arguments ={this.characteristic(), this.classInfo(),
			            ((PolynomialRC) val).characteristic(), val.classInfo()};
				throw new FFaplAlgebraicException(arguments, IAlgebraicError.CHARACTERISTIC_UNEQUAL);
			}
			gf = this.clone();
			gf.setValue((PolynomialRC)val);

		}else if(val instanceof Polynomial){
			gf = this.clone();
			gf.setValue((Polynomial)val);
		}else if (val instanceof ResidueClass) {
			if(! this.characteristic().equals(((ResidueClass) val).modulus())){
				Object[] arguments ={this.characteristic(), this.classInfo(),
			            ((ResidueClass) val).modulus(), val.classInfo()};
				throw new FFaplAlgebraicException(arguments, IAlgebraicError.CHARACTERISTIC_UNEQUAL);
			}
			gf = this.clone();
			gf.setValue(new Polynomial(((ResidueClass) val).value(), ZERO, _thread));
		}else{
			String[] arguments = {"GaloisField equals operation try to compare " +
					this.classInfo() + " with " + val.classInfo()};
			throw new FFaplAlgebraicException(arguments ,IAlgebraicError.INTERNAL);
		}

		return this.value().equals(gf.value());
	}

	@Override
	public String toString(){
		return Polynomial.plyToString(_value);// + " in GF(" + _p + ", " + Polynomial.plyToString(_irrply) + ")";
	}

	@Override
	public GaloisField clone(){
		try {
			GaloisField gf = new GaloisField(_p, this._irrply.clone(), _thread);
			gf.setValue(_value.clone());
			return gf;
		} catch (FFaplAlgebraicException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the multiplicative inverse of <Code>val</Code>
	 * @param val
	 * @return the multiplicative inverse of <Code>val</Code>
	 * @throws FFaplAlgebraicException
	 */
	public static GaloisField inverse(GaloisField val) throws FFaplAlgebraicException{
		GaloisField result;
		PolynomialRC[] tmp;

		if(val.value().isZero()){
			Object[] arguments ={val.value(), val.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.NO_MULTINVERSE);
		}

		tmp = (PolynomialRC[]) Algorithm.eea(val.value(), val.irrPolynomial());
		//tmp = [d, s, t] .... d = s*g + t*h
		if(! tmp[0].isOne()){
			//d <> 1
			if(tmp[0].degree().compareTo(ZERO) > 0){
				Object[] arguments ={"Error in GF inverse - EEA"};
				throw new FFaplAlgebraicException(arguments, IAlgebraicError.INTERNAL);
			}
		}
		result = new GaloisField(val.characteristic(), val.irrPolynomial(), val.getThread());
		result.setValue(tmp[1]);
		return result;
	}

	@Override
	public int typeID() {
		return IJavaType.GALOISFIELD;
	}

	@Override
	public String classInfo() {
		return "GF(" + _p + ", " + Polynomial.plyToString(_irrply) + ")";
	}

	/**
	 * @return the thread within the GaloisField is created
	 */
	public Thread getThread(){
		return _thread;
	}

	/**
	 * Negate the value of the GaloisField
	 * @return the negation of the value of the GaloisField
	 * @throws FFaplAlgebraicException
	 */
	public GaloisField negate() throws FFaplAlgebraicException {
		GaloisField gf = this.clone();
		gf.multiply(ONE.negate());
		return gf;
	}

	/**
	 * Modulo operation
	 * @param gf
	 * @throws FFaplAlgebraicException
	 */
	public void mod(GaloisField gf) throws FFaplAlgebraicException {
		if(! this.equalGF(gf)){
			Object[] arguments ={this.classInfo(), gf.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.TYPES_INCOMPATIBLE);
		}
    	_value.mod(gf.value());
    	_value.mod(_irrply);

	}


	@Override
	public boolean equalType(Object type) {
		if(type instanceof GaloisField){
			try {
				return equalGF((GaloisField) type);
			} catch (FFaplAlgebraicException e) {
				//
			}
		}
		return false;
	}

	@Override
	public int compareTo(GaloisField gf) {
		try {
			if(equals(gf)){
				return 0;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		int val = _value.degree().compareTo(gf.value().degree());
		if(val == 0){
			val = ((Integer)_value.polynomial().size()).compareTo(gf.value().polynomial().size());
			if(val == 0){
				return -1;
			}
		}
		return val;
	}

	/**
	 * Evaluates this element's polynomial at the point x = val.
	 *
	 * @param val
	 * @return the result of the polynomial if x = val
	 * @throws FFaplAlgebraicException
	 */
	public BigInteger calculate(BInteger val) throws FFaplAlgebraicException {
		return _value.calculate(val);
	}

	/**
	 * Checks if a given polynomial's characteristic matches this element's one.
	 * Note that this will return true if the polynomial has no characteristic attached.
	 *
	 * @param ply the polynomial to check
	 * @return true if the characteristics match
	 * @throws FFaplAlgebraicException if a mismatch occurs
	 */
	public boolean characteristicMatches(Polynomial ply) throws FFaplAlgebraicException {
		if (ply instanceof PolynomialRC && !_p.equals(((PolynomialRC) ply).characteristic())) {
			Object[] arguments = {this.characteristic(), this.classInfo(),
					((PolynomialRC) ply).characteristic(), ply.classInfo()};
			throw new FFaplAlgebraicException(arguments, IAlgebraicError.CHARACTERISTIC_UNEQUAL);
		}
		return true;
	}

    @Override
    public GaloisField addR(GaloisField summand) throws FFaplAlgebraicException {
        GaloisField sum = this.clone();
        sum.add(summand);
        return sum;
    }

    @Override
    public GaloisField subR(GaloisField subtrahend) throws FFaplAlgebraicException {
        GaloisField difference = this.clone();
        difference.subtract(subtrahend);
        return difference;
    }

    @Override
    public GaloisField multR(GaloisField factor) throws FFaplAlgebraicException {
        GaloisField product = this.clone();
        product.multiply(factor);
        return product;
    }

    @Override
    public GaloisField scalarMultR(BigInteger factor) throws FFaplAlgebraicException {
        GaloisField product = this.clone();
        product.multiply(factor);
        return product;
    }

    @Override
    public GaloisField divR(GaloisField divisor) throws FFaplAlgebraicException {
        GaloisField quotient = this.clone();
        quotient.divide(divisor);
        return quotient;
    }

    @Override
    public GaloisField negateR() throws FFaplAlgebraicException {
        return this.negate();
    }

    @Override
    public GaloisField powR(BigInteger exponent) throws FFaplAlgebraicException {
        GaloisField power = this.clone();
        power.pow(exponent);
        return power;
    }

	/**
	 * Checks whether a is a primitive root (primitive element) in GF(p^n)
	 * by checking for each distinct prime factor {@code q} of {@code p-1}
	 * if a<sup>(p-1)/q</sup> != 1 (mod p)
	 *
     * @return true, if a is a primitive value of p
     */
    public boolean isPrimitiveRoot(Map<BigInteger, BigInteger> factorsOfPMinusOne)
            throws FFaplAlgebraicException {

        // zero is not a primitive root
        if (this.value().isZero())
            return false;

        BigInteger pMinusOne = this.characteristic().subtract(ONE);

        // factor p-1 (if not already supplied by callee)
		if (factorsOfPMinusOne == null)
			factorsOfPMinusOne = Algorithm.FactorInteger(new BInteger(pMinusOne, _thread));

		// for each distinct prime factor q...
		for (Map.Entry<BigInteger, BigInteger> distinctPrime : factorsOfPMinusOne.entrySet()) {
			// ... check if a^((p-1)/q) != 1
			if (!distinctPrime.getKey().equals(ONE)) {
				if (this.powR(pMinusOne.divide(distinctPrime.getKey())).value().isOne()) {
					return false;
				}
			}
		}

		return true;
	}

	public GaloisField getPrimitiveRoot() throws FFaplAlgebraicException {

		BigInteger p = this.characteristic(),
				n = this.irrPolynomial().degree(),
				r = p.pow(n.intValue()).subtract(ONE).divide(p.subtract(ONE));

		// factorize r, if necessary
		TreeMap<BigInteger, BigInteger> factorsOfR = Algorithm.FactorInteger(new BInteger(r, _thread));
		// factorize p-1
		TreeMap<BigInteger, BigInteger> factorsOfPMinusOne = Algorithm.FactorInteger(new BInteger(p.subtract(ONE), _thread));

		// create instance with value "x"
		GaloisField x = new GaloisField(p, this.irrPolynomial(), _thread);
		// x = 1 * x^1
		x.setValue(new Polynomial(ONE, ONE, _thread));

		if (this.irrPolynomial().isPrimitivePolynomial(factorsOfR, factorsOfPMinusOne, _thread)) {
			// the irreducible polynomial of this field is primitive
			// -> field element x is primitive
			return x;
		} else {
			throw new FFaplAlgebraicException(new Object[0], IAlgebraicError.NOT_IMPLEMENTED);

			// polynomial is not primitive. find one that is
			//PolynomialRCPrime f = Algorithm.getPrimitivePolynomial(p, n, factorsOfR, factorsOfPMinusOne, _thread);

			// then convert the element "x" from that field to this field
			// TODO convert field elements
		}
	}
}
