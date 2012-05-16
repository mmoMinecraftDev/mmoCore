/*
 * This file is part of mmoCore <http://github.com/mmoMinecraftDev/mmoCore>.
 *
 * mmoCore is free software: you can redistribute it and/or modify
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
package mmo.Core.PartyAPI;

import java.util.List;

import org.bukkit.entity.Player;

public interface Party {
	/**
	 * Find a Party via a Player.
	 * @param player the player who's party we are trying to find
	 * @return the Party they are a member of
	 */
	public Party find(Player player);

	/**
	 * Find a Party via a player name.
	 * @param player the player who's party we are trying to find
	 * @return the Party they are a member of
	 */
	public Party find(String player);

	/**
	 * Checks whether a player is in this party.
	 * @param player the player to check
	 * @return if they're both in the same party
	 */
	public boolean contains(Player player);

	/**
	 * Returns whether this is a real party, or a single player with possible invites.
	 * @return if there are party members other than the leader
	 */
	public boolean isParty();

	/**
	 * Returns whether this party has any invites.
	 * @return if there are invites outstanding
	 */
	public boolean hasInvites();

	/**
	 * Find all outstanding invites for a Player.
	 * @param player The Player we're interested in
	 * @return A List of parties that have invited player
	 */
	public List<Party> findInvites(Player player);

	/**
	 * Remove all outstanding invites for a Player.
	 * This is for all parties, and not just this one.
	 * @param player the Player to un-invite
	 */
	public void declineInvites(Player player);

	/**
	 * Get online players in a party, exclude a single player from the list.
	 * @param name the player to exclude from the list
	 * @return a list of all online players
	 */
	public List<Player> getMembers(String name);

	/**
	 * Get online players in a party, exclude a single player from the list.
	 * @param player the player to exclude from the list
	 * @return a list of all online players
	 */
	public List<Player> getMembers(Player player);

	/**
	 * Get online players in a party.
	 * @return a list of all online players
	 */
	public List<Player> getMembers();

	/**
	 * Get all players in a party.
	 * @return a list of player names separated by commas
	 */
	public String getMemberNames();

	/**
	 * Get all players invited to a party.
	 * @return a list of player names separated by commas
	 */
	public String getInviteNames();

	/**
	 * Get the number of members in this Party.
	 * @return the number of members
	 */
	public int size();

	/**
	 * Adds an invited online Player to this Party.
	 * @param player the player to add
	 * @return if they have been successfully added
	 */
	public boolean accept(Player player);

	/**
	 * Removes an invite for a Player.
	 * @param player the Player to un-invite
	 * @return if they had a valid invite or not
	 */
	public boolean decline(Player player);

	/**
	 * Delete a Player from a Party.
	 * This removes the Player from this party
	 * and will delete the party if it is now empty.
	 * @param name the player to remove from this party
	 * @return if they have been found and removed
	 */
	public boolean remove(String name);

	/**
	 * Promote a Player to party leader.
	 * @param leader the person attempting it
	 * @param name   the player to promote
	 * @return if they have been promoted
	 */
	public boolean promote(Player leader, String name);

	/**
	 * Leave a party (in a friendly way).
	 * @param player the Player leaving
	 * @return if they left this party
	 */
	public boolean leave(Player player);

	/**
	 * Leave a party (in an unfriendly way).
	 * @param leader the Player attempting to do this
	 * @param name   the Player leaving
	 * @return if they left this party
	 */
	public boolean kick(Player leader, String name);

	/**
	 * Determines if the Player is able to invite / kick etc.
	 * @param player the player to check
	 * @return if they are the party leader or not
	 */
	public boolean isLeader(Player player);

	/**
	 * Determines if the Player is able to invite / kick etc.
	 * @param name the player to check
	 * @return if they are the party leader or not
	 */
	public boolean isLeader(String name);

	/**
	 * Get the party leader.
	 * @return leader name
	 */
	public String getLeader();

	/**
	 * Invite a player to the party.
	 * @param leader the Player attempting to do this
	 * @param name   the player to invite
	 * @return if the invitation was successful
	 */
	public boolean invite(Player leader, String name);

	/**
	 * Update all party members.
	 */
	public void update();

	/**
	 * Update a single player's party display
	 * @param player
	 */
	public void update(Player player);

	/**
	 * Print current party status.
	 * @param player who we send it to
	 */
	public void status(Player player);
}
