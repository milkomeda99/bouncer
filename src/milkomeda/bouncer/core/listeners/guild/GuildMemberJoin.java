package milkomeda.bouncer.core.listeners.guild;

import milkomeda.bouncer.core.data.util.GuildTable;
import milkomeda.bouncer.core.data.util.UserBanTable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Class listener for when a member joins the guild. Purpose is to update the role of a member.
 *
 * @author Kevin Mattix
 * @date 11-10-2020
 */
public class GuildMemberJoin extends ListenerAdapter{

	private final GuildTable GT;
	private final UserBanTable UBT;

	/**
	 * @param gt Database connection.
	 */
	public GuildMemberJoin(GuildTable gt, UserBanTable ubt){
		GT = gt;
		UBT = ubt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event){
		Guild guild = event.getGuild();
		if(UBT.isBanned(event.getUser().getIdLong(), guild.getIdLong())){
			guild.kick(event.getMember()).queue();
			return;
		}
		if(GT.getRoleID(guild.getIdLong()) != 0){
			Role autoRole = event.getGuild().getRoleById(GT.getRoleID(guild.getIdLong()));
			try{
				assert autoRole != null;
				event.getGuild().addRoleToMember(event.getMember(), autoRole).queue();
			}catch(HierarchyException e){
				guild.getOwner().getUser().openPrivateChannel().flatMap(pChannel -> pChannel.sendMessage(
						String.format("Verify the bouncer role is above your '%s' auto role in %s. " +
								"%s was not added to the auto role.",
								autoRole.getName(), GT.getGuildName(guild.getIdLong()), event.getUser().getAsTag())))
						.queue();
			}
		}
	}
}
