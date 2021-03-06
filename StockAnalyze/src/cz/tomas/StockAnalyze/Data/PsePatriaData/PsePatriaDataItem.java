/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

/**
 * @author tomas
 *
 */
public class PsePatriaDataItem {
	String name;
	float value;
	float percentableChange;
	String link;
	boolean isIndex;
	
	/**
	 * name of equity
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * set name of equity
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * get price value
	 * @return the value
	 */
	public float getValue() {
		return value;
	}
	/**
	 * set price value
	 * @param value the value to set
	 */
	public void setValue(float value) {
		this.value = value;
	}
	/**
	 * get percentable price change from last close
	 * @return the percentableChange
	 */
	public float getPercentableChange() {
		return percentableChange;
	}
	/**
	 * set percentable price change from last close
	 * @param percentableChange the percentableChange to set
	 */
	public void setPercentableChange(float percentableChange) {
		this.percentableChange = percentableChange;
	}
	/**
	 * url link to more detailed information
	 * @return the link
	 */
	public String getLink() {
		return link;
	}
	/**
	 * url link to more detailed information
	 * @param link the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}
	/**
	 * true if this isn't equity, but stock index
	 * @return the isIndex
	 */
	public boolean isIndex() {
		return isIndex;
	}
	/**
	 * true if this isn't equity, but stock index
	 * @param isIndex the isIndex to set
	 */
	public void setIndex(boolean isIndex) {
		this.isIndex = isIndex;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isIndex ? 1231 : 1237);
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Float.floatToIntBits(percentableChange);
		result = prime * result + Float.floatToIntBits(value);
		return result;
	}
	/* (non-Javadoc)
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
		PsePatriaDataItem other = (PsePatriaDataItem) obj;
		if (isIndex != other.isIndex)
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Float.floatToIntBits(percentableChange) != Float
				.floatToIntBits(other.percentableChange))
			return false;
		if (Float.floatToIntBits(value) != Float.floatToIntBits(other.value))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PsePatriaDataItem [name=" + name + ", value=" + value
				+ ", percentableChange=" + percentableChange + ", link=" + link
				+ ", isIndex=" + isIndex + "]";
	}
	
	
}
