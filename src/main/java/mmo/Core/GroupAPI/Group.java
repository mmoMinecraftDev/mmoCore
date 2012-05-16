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
package mmo.Core.GroupAPI;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;

public interface Group {
	/**
	 * Find a Group by it's id.
	 * @param id the path.to.the.group
	 * @return a single group which matches the id or null
	 */
	public Group findGroup(String id);

	/**
	 * Get the parent of this group, may return null for the root "Group".
	 * @return the parent group or null
	 */
	public Group getParent();

	/**
	 * Check if one group is an ancestor of this one.
	 * @param group the parent or higher to check
	 * @return if it is an ancestor
	 */
	public boolean isParent(Group group);

	/**
	 * Get all children of this group, may return an empty set.
	 * @return a st of groups
	 */
	public Set<Group> getChildren();

	/**
	 * Check if a group is a descendant of this group.
	 * @param group the group to check
	 * @return if it is a descendant
	 */
	public boolean isChild(Group group);

	/**
	 * A set of groups that are required to become a member of this group.
	 * @return all required groups
	 */
	public Set<Group> require();

	/**
	 * A set of groups that exclude a member from joining this group.
	 * For "unique" groups (faction, sex etc) this would be every other direct descendant of the parent.
	 * @return all excluded groups
	 */
	public List<Group> exclude();

	/**
	 * Find all groups this player is a member of.
	 * @param player the Player or player name to find
	 * @return a set of groups
	 */
	public <T> Set<Group> find(T player);

	/**
	 * Find groups this player is a member of that are <b>direct<b> descendants of a single group.
	 * @param player the Player or player name to find
	 * @param parent the parent group to check
	 * @return a set of groups
	 */
	public <T> Set<Group> find(T player, Group parent);

	/**
	 * Find groups this player is a member of that are descendants of a single group.
	 * @param player the Player or player name to find
	 * @param parent the parent group to check
	 * @param depth  how deep into the tree should we look
	 * @return a set of groups
	 */
	public <T> Set<Group> find(T player, Group parent, int depth);

	/**
	 * Check if the player is a member of this group or any descendants.
	 * @param player the Player or player name to find
	 * @return if they are a member
	 */
	public <T> boolean isMember(T player);

	/**
	 * Check if the player is a member of this group (ignore descendants).
	 * @param player the Player or player name to find
	 * @return if they are a member
	 */
	public <T> boolean isOwnMember(T player);

	/**
	 * Check if a player can invite another player into this group.
	 * @param inviter the Player or player name inviting
	 * @param invitee the Player or player name being invited
	 * @return if they can invite
	 */
	public <T> boolean canInvite(T inviter, T invitee);

	/**
	 * Invite a player into this group.
	 * @param inviter the Player or player name inviting
	 * @param invitee the Player or player name being invited
	 * @return if they have been invited
	 */
	public <T> boolean invite(T inviter, T invitee);

	/**
	 * Check if a player has an outstanding invitation for this group.
	 * @param player the Player or player name checking
	 * @return if they have an invitation
	 */
	public <T> boolean canAccept(T player);

	/**
	 * Accept an outstanding invitation for this group.
	 * @param player the Player or player name accepting
	 * @return if they have accepted an invitation
	 */
	public <T> boolean accept(T player);

	/**
	 * Check if a player has an outstanding invitation for this group.
	 * @param player the Player or player name checking
	 * @return if they have an invitation
	 */
	public <T> boolean canDecline(T player);

	/**
	 * Decline an outstanding invitation for this group.
	 * @param player the Player or player name declining
	 * @return if they have declined an invitation
	 */
	public <T> boolean decline(T player);

	/**
	 * Check if a player can join this group without an invitation.
	 * @param player the Player or player name to check
	 * @return if they can join
	 */
	public <T> boolean canJoin(T player);

	/**
	 * Join this group without an invitation.
	 * @param player the Player or player name joining
	 * @return if they have joined
	 */
	public <T> boolean join(T player);

	/**
	 * Check if a player can leave this group.
	 * @param player the Player or player name wanting to leave
	 * @return if they can leave
	 */
	public <T> boolean canLeave(T player);

	/**
	 * Leave this group.
	 * @param player the Player or player name leaving
	 * @return if they have left
	 */
	public <T> boolean leave(T player);

	/**
	 * Check if one player can kick another from this group.
	 * @param leader the Player or player name wanting to kick
	 * @param player the Player or player name being kicked
	 * @return if they can kick
	 */
	public <T> boolean canKick(T leader, T player);

	/**
	 * Kick a player from this group.
	 * @param leader the Player or player name wanting to kick
	 * @param player the Player or player name being kicked
	 * @return if they have been kicked
	 */
	public <T> boolean kick(T leader, T player);

	/**
	 * Check if one player can promote another in this group.
	 * @param leader the Player or player name wanting to promote
	 * @param player the Player or player name being promoted
	 * @return if they can promote
	 */
	public <T> boolean canPromote(T leader, T player);

	/**
	 * Promote a player in this group.
	 * @param leader the Player or player name wanting to promote
	 * @param player the Player or player name being promoted
	 * @return if they can promote
	 */
	public <T> boolean promote(T leader, T player);

	/**
	 * Check if one player can demote another in this group.
	 * @param leader the Player or player name wanting to demote
	 * @param player the Player or player name being demoted
	 * @return if they can demote
	 */
	public <T> boolean canDemote(T leader, T player);

	/**
	 * Demote a player in this group.
	 * @param leader the Player or player name wanting to demote
	 * @param player the Player or player name being demoted
	 * @return if they can promote
	 */
	public <T> boolean demote(T leader, T player);

	/**
	 * Get the id for this group.
	 * This is the full dot.separated path to this group, starting with "group."
	 * @return the group id
	 */
	public String getId();

	/**
	 * Get the public name for this group.
	 * This may not be unique (unlike the id).
	 * @return the group name
	 */
	public String getName();

	/**
	 * Get a set of all members of this group and all descendants.
	 * @return a set of player names
	 */
	public Set<String> getMembers();

	/**
	 * Get a set of all members of this group only.
	 * @return a set of player names
	 */
	public Set<String> getOwnMembers();

	/**
	 * Get the category name for this group.
	 * @return the group category
	 */
	public String getCategory();

	/**
	 * Set a string for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param value  the value to set
	 */
	public <T> void setString(T player, String key, String value);

	/**
	 * Get a string for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param def	the default value if not found
	 * @return the data
	 */
	public <T> String getString(T player, String key, String def);

	/**
	 * Set a list of strings for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param value  the value to set
	 */
	public <T> void setStringList(T player, String key, List<String> value);

	/**
	 * Get a list of strings for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param def	the default value if not found
	 * @return the data
	 */
	public <T> List<String> getStringList(T player, String key, List<String> def);

	/**
	 * Set an integer for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param value  the value to set
	 */
	public <T> void setInt(T player, String key, int value);

	/**
	 * Get an integer for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param def	the default value if not found
	 * @return the data
	 */
	public <T> int getInt(T player, String key, int def);

	/**
	 * Set a double for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param value  the value to set
	 */
	public <T> void setDouble(T player, String key, double value);

	/**
	 * Get a double for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param def	the default value if not found
	 * @return the data
	 */
	public <T> double getDouble(T player, String key, double def);

	/**
	 * Set a boolean for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param value  the value to set
	 */
	public <T> void setBoolean(T player, String key, boolean value);

	/**
	 * Get a boolean for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param def	the default value if not found
	 * @return the data
	 */
	public <T> boolean getBoolean(T player, String key, boolean def);

	/**
	 * Set a Location for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param value  the value to set
	 */
	public <T> void setLocation(T player, String key, Location value);

	/**
	 * Get a Location for a player in this group.
	 * @param player the Player (or player name) this relates to
	 * @param key	a unique id (per group per player)
	 * @param def	the default value if not found
	 * @return the data
	 */
	public <T> Location getLocation(T player, String key, Location def);
}
