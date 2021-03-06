package AccountEdit;

import AccountList.AccountListController;
import controller.AbstractController;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Account.Account;
import model.Account.AccountModel;
import util.Currency;
import util.StageUtil;
import view.View;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * @author sangm (sang.mercado@gmail.com)
 */
public class EditController extends AbstractController implements Initializable {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(?:0|[1-9\\.]\\d{0,2}(?:,?\\d{3})*)(?:\\.\\d+)?$");
    private static final Pattern FIX_NUMBER_PATTERN = Pattern.compile("[^\\d+\\.]+");

    private AccountListController parentController;
    private Account account;
    private Currency currency;

    /* FXML */
    public TextField amountField;
    public Label accountId;
    public Label accountName;
    public Label accountCurrency;
    public Label accountBalance;
    public Button depositButton;
    public Button withdrawButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resetAmountField();
        amountField.textProperty().addListener((observer, oldValue, newValue) -> {
            ObservableList<String> styles = amountField.getStyleClass();
            if (verifyAmount(amountField.getText())) {
                if (styles.contains("btn-danger")) {
                    styles.remove("btn-danger");
                }
            } else {
                if (!styles.contains("btn-danger")) {
                    styles.add("btn-danger");
                }
            }
        });
    }


    public void init(AccountListController accountListController, AccountModel model, Currency currency) {
        initModel(model);
        setAccount(model.getCurrentAccount());
        setCurrency(currency);
        setParentController(accountListController);
    }

    private void setAccount(Account account) {
        this.account = account;
        StageUtil.setAccountLabels(account, accountId, accountName, accountBalance);
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        if (currency == Currency.US) {
            this.accountCurrency.setText("US");
        } else if (currency == Currency.EURO) {
            this.accountCurrency.setText("EURO");
        } else if (currency == Currency.YEN) {
            this.accountCurrency.setText("YEN");
        }
    }

    public void setParentController(AccountListController parentController) {
        this.parentController = parentController;
    }

    public void depositButtonHandler(ActionEvent actionEvent) {
        String unverifiedText = amountField.getText();

        if (verifyAmount(unverifiedText)) {
            String depositString = FIX_NUMBER_PATTERN.matcher(amountField.getText()).replaceAll("");
            Double unconvertedDepositAmount = Double.parseDouble(depositString);
            Double depositAmount = Currency.convert(unconvertedDepositAmount, this.currency);

            account.deposit(depositAmount);
            resetAmountField();
        }
    }

    public void withdrawButtonHandler(ActionEvent actionEvent) throws IOException {
        String fieldText = amountField.getText();

        if (verifyAmount(fieldText)) {
            String withdrawString = FIX_NUMBER_PATTERN.matcher(fieldText).replaceAll("");
            Double rawWithdrawAmount = Double.parseDouble(withdrawString);
            Double withdrawAmount = Currency.convert(rawWithdrawAmount, this.currency);
            try {
                account.withdraw(withdrawAmount);
            } catch (IllegalArgumentException e) {
                /** What requirements asks me to do
                 * controller must request the view to open a pop-up window that contains message
                 * “Insufficient funds: amount to withdraw is x, it is greater than available funds: y”.
                 * The pop-up window contains button “Dismiss” on pressing which the pop-up window should close.
                 */

                View errorView = StageUtil.generateErrorView(e.getMessage(), 450, 200);
                StageUtil.initStage(errorView, 450, 200).show();
            } finally {
                resetAmountField();
            }
        }
    }

    private boolean verifyAmount(String amountText) {
        return NUMBER_PATTERN.matcher(amountText).find();
    }

    private void resetAmountField() {
        amountField.setText("0.0");
        Platform.runLater(() -> {
            amountField.requestFocus();
        });
    }

}
