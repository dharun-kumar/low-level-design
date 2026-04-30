package service;

import entity.Expense;
import entity.Group;
import entity.Split;
import entity.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SplitwiseService {

    private static SplitwiseService INSTANCE;

    private Map<String, User> allUsers;
    private Map<String, Group> groups;

    private SplitwiseService() {
        allUsers = new ConcurrentHashMap<>();
        groups = new ConcurrentHashMap<>();
    }

    public static synchronized SplitwiseService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new SplitwiseService();
        }
        return INSTANCE;
    }

    public String createGroup(String name) {
        Group group = new Group(name);
        groups.put(group.getGroupID(), group);
        return group.getGroupID();
    }

    public String createUser(String name, String email) {
        User user = new User(name, email);
        allUsers.put(user.getUserID(), user);
        return user.getUserID();
    }

    public void addParticipants(String groupID, Set<String> participantsID) {
        Set<User> participants = new HashSet<>();
        for(String participantID : participantsID) {
            participants.add(getUserByID(participantID));
        }
        getGroupByID(groupID).addMembers(participants);
    }

    public void addExpense(String groupID, Expense expense) {
        for(User participant: expense.getParticipants()) {
            if(!getGroupByID(groupID).getMembers().contains(participant)) {
                throw new IllegalArgumentException("Participant with ID " + participant.getUserID() + " is not part of the group with ID " + groupID);
            }
        }

        for(Split split : expense.getSplits()) {
            expense.getPaidBy().getBalance().adjustBalance(split.getParticipant(), split.getAmount());   //owes to paid user
            split.getParticipant().getBalance().adjustBalance(expense.getPaidBy(), -(split.getAmount()));   //owed by participant user
        }

        getGroupByID(groupID).addExpense(expense);
    }

    public void settleUp(String senderID, String receiverID, double amount) {
        getUserByID(senderID).getBalance().adjustBalance(getUserByID(receiverID), amount);
        getUserByID(receiverID).getBalance().adjustBalance(getUserByID(senderID), -(amount));
    }

    public void displayGroupBalance(String groupID) {
        for(User user : getGroupByID(groupID).getMembers()) {
            user.getBalance().displayBalance(getGroupByID(groupID).getMembers());
        }
    }

    public void displayUserBalance(String userID) {
        getUserByID(userID).getBalance().displayBalance(null);
    }

    public synchronized void removeUser(String groupID, String userID) {
        if(!getGroupByID(groupID).getMembers().contains(getUserByID(userID))) {
            throw new IllegalArgumentException("User with ID " + userID + " is not part of the group with ID " + groupID);
        }
        getGroupByID(groupID).removeMember(getUserByID(userID));
    }

    public User getUserByID(String userID) {
        return allUsers.get(userID);
    }

    public Group getGroupByID(String groupID) {
        return groups.get(groupID);
    }

    public void simplifyDebts(String groupID) {
        Map<String, Double> netBalances = new HashMap<>();

        for(User user : getGroupByID(groupID).getMembers()) {
            double netBalance = 0;

            // calculating net only with group members
            for(Map.Entry<User, Double> entry : user.getBalance().getAllBalances().entrySet()) {
                if(getGroupByID(groupID).getMembers().contains(entry.getKey())) {
                    netBalance += entry.getValue();
                }
            }
            netBalances.put(user.getUserID(), netBalance);
        }

        List<Map.Entry<String, Double>> creditors = netBalances.entrySet().stream()
                .filter(e -> e.getValue() > 0.01)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();

        List<Map.Entry<String, Double>> debtors = netBalances.entrySet().stream()
                .filter(e -> e.getValue() < -0.01)
                .sorted(Map.Entry.comparingByValue())
                .toList();


        int i=0, j=0;
        while(i < creditors.size() && j < debtors.size()) {

            Map.Entry<String, Double> creditor = creditors.get(i);
            Map.Entry<String, Double> debtor = debtors.get(j);

            double minSettleAmount = Math.min(creditor.getValue(), Math.abs(debtor.getValue()));

            System.out.printf("%s pays %s : %.2f%n", getUserByID(debtor.getKey()).getName(), getUserByID(creditor.getKey()).getName(), minSettleAmount);

            creditor.setValue(creditor.getValue() - minSettleAmount);
            debtor.setValue(debtor.getValue() + minSettleAmount);

            if(Math.abs(creditor.getValue()) < 0.01) {
                i++;
            }
            if(Math.abs(debtor.getValue()) < 0.01) {
                j++;
            }

        }
    }

}
