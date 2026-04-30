package entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Group {

    private final UUID groupID;
    private final String name;
    private final Set<User> members;
    private final List<Expense> expenses;

    public Group(String name) {
        this.groupID = UUID.randomUUID();
        this.name = name;
        this.members = ConcurrentHashMap.newKeySet();
        this.expenses = Collections.synchronizedList(new ArrayList<>());
    }

    public void addMember(User member) {
        this.members.add(member);
    }

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
    }

    public void removeMember(User member) {
        for(Map.Entry<User, Double> balance : member.getBalance().getAllBalances().entrySet()) {
            if(!members.contains(balance.getKey())) {
                continue;
            }

            if(Math.abs(balance.getValue()) > 0.01) {
                throw new RuntimeException("Fail to remove user, Expense were not settle up");
            }
        }
        members.remove(member);
    }

    public String getGroupID() {
        return groupID.toString();
    }

    public String getName() {
        return name;
    }

    public Set<User> getMembers() {
        return members;
    }

}
