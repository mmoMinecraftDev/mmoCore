/*
 * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
 *
 * mmoMinecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Core.util;

import java.util.BitSet;

public class EnumBitSet extends BitSet {
	/**
	 * Sets the bit at the specified index to the complement of its
	 * current value.
	 * @param bitIndex the index of the bit to flip.
	 * @throws IndexOutOfBoundsException if the specified index is negative.
	 */
	public void flip(Enum<?> bitIndex) {
		super.flip(bitIndex.ordinal());
	}

	/**
	 * Sets each bit from the specified <tt>fromIndex</tt> (inclusive) to the
	 * specified <tt>toIndex</tt> (exclusive) to the complement of its current
	 * value.
	 * @param fromIndex index of the first bit to flip.
	 * @param toIndex   index after the last bit to flip.
	 * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
	 *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
	 *                                   larger than <tt>toIndex</tt>.
	 */
	public void flip(Enum<?> fromIndex, Enum<?> toIndex) {
		super.flip(fromIndex.ordinal(), toIndex.ordinal());
	}

	/**
	 * Sets the bit at the specified index to <code>true</code>.
	 * @param bitIndex an enum index.
	 * @throws IndexOutOfBoundsException if the specified index is negative.
	 */
	public void set(Enum<?> bitIndex) {
		super.set(bitIndex.ordinal());
	}

	/**
	 * Sets the bit at the specified index to the specified value.
	 * @param bitIndex a bit index.
	 * @param value	a boolean value to set.
	 * @throws IndexOutOfBoundsException if the specified index is negative.
	 */
	public void set(Enum<?> bitIndex, boolean value) {
		super.set(bitIndex.ordinal(), value);
	}

	/**
	 * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
	 * specified <tt>toIndex</tt> (exclusive) to <code>true</code>.
	 * @param fromIndex index of the first bit to be set.
	 * @param toIndex   index after the last bit to be set.
	 * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
	 *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
	 *                                   larger than <tt>toIndex</tt>.
	 */
	public void set(Enum<?> fromIndex, Enum<?> toIndex) {
		super.set(fromIndex.ordinal(), toIndex.ordinal());
	}

	/**
	 * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
	 * specified <tt>toIndex</tt> (exclusive) to the specified value.
	 * @param fromIndex index of the first bit to be set.
	 * @param toIndex   index after the last bit to be set
	 * @param value	 value to set the selected bits to
	 * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
	 *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
	 *                                   larger than <tt>toIndex</tt>.
	 */
	public void set(Enum<?> fromIndex, Enum<?> toIndex, boolean value) {
		super.set(fromIndex.ordinal(), toIndex.ordinal(), value);
	}

	/**
	 * Sets the bit specified by the index to <code>false</code>.
	 * @param bitIndex the index of the bit to be cleared.
	 * @throws IndexOutOfBoundsException if the specified index is negative.
	 * @since JDK1.0
	 */
	public void clear(Enum<?> bitIndex) {
		super.clear(bitIndex.ordinal());
	}

	/**
	 * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
	 * specified <tt>toIndex</tt> (exclusive) to <code>false</code>.
	 * @param fromIndex index of the first bit to be cleared.
	 * @param toIndex   index after the last bit to be cleared.
	 * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
	 *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
	 *                                   larger than <tt>toIndex</tt>.
	 * @since 1.4
	 */
	public void clear(Enum<?> fromIndex, Enum<?> toIndex) {
		super.clear(fromIndex.ordinal(), toIndex.ordinal());
	}

	/**
	 * Returns the value of the bit with the specified index. The value
	 * is <code>true</code> if the bit with the index <code>bitIndex</code>
	 * is currently set in this <code>BitSet</code>; otherwise, the result
	 * is <code>false</code>.
	 * @param bitIndex the bit index.
	 * @return the value of the bit with the specified index.
	 * @throws IndexOutOfBoundsException if the specified index is negative.
	 */
	public boolean get(Enum<?> bitIndex) {
		return super.get(bitIndex.ordinal());
	}

	/**
	 * Returns a new <tt>BitSet</tt> composed of bits from this <tt>BitSet</tt>
	 * from <tt>fromIndex</tt> (inclusive) to <tt>toIndex</tt> (exclusive).
	 * @param fromIndex index of the first bit to include.
	 * @param toIndex   index after the last bit to include.
	 * @return a new <tt>BitSet</tt> from a range of this <tt>BitSet</tt>.
	 * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
	 *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
	 *                                   larger than <tt>toIndex</tt>.
	 */
	public EnumBitSet get(Enum<?> fromIndex, Enum<?> toIndex) {
		EnumBitSet result = new EnumBitSet();
		result.or(super.get(fromIndex.ordinal(), toIndex.ordinal()));
		return result;
	}

	/**
	 * Returns the index of the first bit that is set to <code>true</code>
	 * that occurs on or after the specified starting index. If no such
	 * bit exists then -1 is returned.
	 * <p/>
	 * To iterate over the <code>true</code> bits in a <code>BitSet</code>,
	 * use the following loop:
	 * <p/>
	 * <pre>
	 * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
	 *     // operate on index i here
	 * }</pre>
	 * @param fromIndex the index to start checking from (inclusive).
	 * @return the index of the next set bit.
	 * @throws IndexOutOfBoundsException if the specified index is negative.
	 */
	public int nextSetBit(Enum<?> fromIndex) {
		return super.nextSetBit(fromIndex.ordinal());
	}

	/**
	 * Returns the index of the first bit that is set to <code>false</code>
	 * that occurs on or after the specified starting index.
	 * @param fromIndex the index to start checking from (inclusive).
	 * @return the index of the next clear bit.
	 * @throws IndexOutOfBoundsException if the specified index is negative.
	 */
	public int nextClearBit(Enum<?> fromIndex) {
		return super.nextClearBit(fromIndex.ordinal());
	}
}
