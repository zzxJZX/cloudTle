package littlec.conference.talk.callback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

import littlec.conference.talk.model.ConferenceMember;


/**
 * @author: fang wei
 * @data: 2017/1/24
 * @Description: <计算新旧成员差异>
 */
public class MemberDiffUtilCallback extends DiffUtil.Callback {

    private List<ConferenceMember> mOldMembers;
    private List<ConferenceMember> mNewMembers;

    public MemberDiffUtilCallback(List<ConferenceMember> mOldMembers, List<ConferenceMember> mNewMembers) {
        this.mOldMembers = mOldMembers;
        this.mNewMembers = mNewMembers;
    }

    @Override
    public int getOldListSize() {
        return mOldMembers != null ? mOldMembers.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return mNewMembers != null ? mNewMembers.size() : 0;
    }


    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     * <p>
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return True if the two items represent the same object or false if they are different.
     * @discription 号码不同时需要刷新item
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldMembers.get(oldItemPosition).getUserName().equals(mNewMembers.get(newItemPosition).getUserName());
    }

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     * <p>
     * DiffUtil uses this method to check equality instead of {@link Object#equals(Object)}
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * {@link android.support.v7.widget.RecyclerView.Adapter RecyclerView.Adapter}, you should
     * return whether the items' visual representations are the same.
     * <p>
     * This method is called only if {@link #areItemsTheSame(int, int)} returns
     * {@code true} for these items.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     *                        oldItem
     * @return True if the contents of the items are the same or false if they are different.
     * @discription 旧数据与新数据SurfaceView不同时要刷新
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ConferenceMember oldMember = mOldMembers.get(oldItemPosition);
        ConferenceMember newMember = mNewMembers.get(newItemPosition);
        return !(oldMember.getVideoView() != null && newMember.getVideoView() != null && oldMember.getVideoView() == newMember.getVideoView() && oldMember.getAudioMute() != newMember.getAudioMute())
                && (oldMember.getVideoView() == null && newMember.getVideoView() == null
                        || oldMember.getVideoView() != null && newMember.getVideoView() != null && oldMember.getVideoView() == newMember.getVideoView())
                && oldMember.getVideoWidth() == newMember.getVideoWidth()
                && oldMember.getStatus().equals(newMember.getStatus());

    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        ConferenceMember oldMember = mOldMembers.get(oldItemPosition);
        ConferenceMember newMember = mNewMembers.get(newItemPosition);
        Bundle bundle = new Bundle();
        if (!oldMember.getStatus().equals(newMember.getStatus()) && !newMember.getStatus().equals("3")) {
            bundle.putString("status", newMember.getStatus());
        }
        if (!bundle.isEmpty()) {
            return bundle;
        }
        return null;
    }
}
