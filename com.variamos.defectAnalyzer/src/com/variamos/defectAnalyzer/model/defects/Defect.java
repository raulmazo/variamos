package com.variamos.defectAnalyzer.model.defects;

import java.util.ArrayList;
import java.util.List;

import com.cfm.hlcl.BooleanExpression;
import com.variamos.defectAnalyzer.model.enums.DefectType;

public class Defect {

	protected String id;
	protected DefectType defectType;
	// Guarda la lista de restricciones que se usan para identificar el defecto
	// con las operaciones de verificación
	protected List<BooleanExpression> verificationExpressions;

	public Defect() {
		super();
		verificationExpressions = new ArrayList<BooleanExpression>();
	}

	public Defect(BooleanExpression verificationExpression) {
		super();
		verificationExpressions.add(verificationExpression);

	}

	public Defect(List<BooleanExpression> verificationExpressions) {
		super();
		this.verificationExpressions = verificationExpressions;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the defectType
	 */
	public DefectType getDefectType() {
		return defectType;
	}

	/**
	 * @param defectType
	 *            the defectType to set
	 */
	public void setDefectType(DefectType defectType) {
		this.defectType = defectType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((defectType == null) ? 0 : defectType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Defect other = (Defect) obj;
		if (defectType != other.defectType)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Defect [id=" + id + ", defectType=" + defectType + "]";
	}

	/**
	 * @return the verificationExpression
	 */
	public BooleanExpression getVerificationExpression() {

		if (!verificationExpressions.isEmpty()) {
			return verificationExpressions.get(0);
		} else {
			return null;
		}

	}

	/**
	 * @param verificationExpression
	 *            the verificationExpression to set
	 */
	public void setVerificationExpression(
			BooleanExpression verificationExpression) {
		verificationExpressions.add(verificationExpression);
	}

	public List<BooleanExpression> getVerificationExpressions() {
		return verificationExpressions;
	}

	public void setVerificationExpressions(
			List<BooleanExpression> verificationExpressions) {
		this.verificationExpressions = verificationExpressions;
	}

}
