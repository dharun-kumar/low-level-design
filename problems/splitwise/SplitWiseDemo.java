import entity.Expense;
import service.SplitwiseService;
import split.EqualSplit;
import split.ExactSplit;
import split.PercentSplit;

import java.util.List;
import java.util.Set;

public class SplitWiseDemo {

    public static void main() {

        SplitwiseService splitwiseService = SplitwiseService.getInstance();

        String aliceID = splitwiseService.createUser("Alice", "alice@gmail.com");
        String bobID = splitwiseService.createUser("Bob", "bob@gmail.com");
        String charlieID = splitwiseService.createUser("Charlie", "charlie@gmail.com");

        String groupID = splitwiseService.createGroup("Trip to Goa");
        splitwiseService.addParticipants(groupID, Set.of(aliceID, bobID, charlieID));

        Expense expense = new Expense.Builder()
                .description("Hotel")
                .amount(900)
                .paidBy(splitwiseService.getUserByID(aliceID))
                .participants(List.of(splitwiseService.getUserByID(aliceID), splitwiseService.getUserByID(bobID), splitwiseService.getUserByID(charlieID)))
                .splitStrategy(new EqualSplit())
                .splitValues(null)
                .build();
        splitwiseService.addExpense(groupID, expense);

        splitwiseService.displayGroupBalance(groupID);
        System.out.println();

        expense = new Expense.Builder()
                .description("Food")
                .amount(300)
                .paidBy(splitwiseService.getUserByID(bobID))
                .participants(List.of(splitwiseService.getUserByID(aliceID), splitwiseService.getUserByID(bobID), splitwiseService.getUserByID(charlieID)))
                .splitStrategy(new ExactSplit())
                .splitValues(List.of(100.0, 100.0, 100.0))
                .build();
        splitwiseService.addExpense(groupID, expense);

        splitwiseService.displayGroupBalance(groupID);
        System.out.println();

        expense = new Expense.Builder()
                .description("Sightseeing")
                .amount(600)
                .paidBy(splitwiseService.getUserByID(charlieID))
                .participants(List.of(splitwiseService.getUserByID(aliceID), splitwiseService.getUserByID(bobID), splitwiseService.getUserByID(charlieID)))
                .splitStrategy(new PercentSplit())
                .splitValues(List.of(34.0, 33.0, 33.0))
                .build();
        splitwiseService.addExpense(groupID, expense);

        splitwiseService.displayGroupBalance(groupID);
        System.out.println();

        System.out.println("Simplified debt ");
        splitwiseService.simplifyDebts(groupID);
        System.out.println();

        splitwiseService.settleUp(bobID, aliceID, 296);
        splitwiseService.settleUp(bobID, charlieID, 2);

        splitwiseService.displayGroupBalance(groupID);
        System.out.println();

        //splitwiseService.removeUser(groupID, aliceID);

    }

}
