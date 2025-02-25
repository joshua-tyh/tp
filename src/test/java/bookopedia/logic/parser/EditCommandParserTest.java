package bookopedia.logic.parser;

import static bookopedia.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static bookopedia.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static bookopedia.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static bookopedia.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static bookopedia.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static bookopedia.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static bookopedia.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static bookopedia.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static bookopedia.logic.commands.CommandTestUtil.INVALID_PARCEL_DESC;
import static bookopedia.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static bookopedia.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static bookopedia.logic.commands.CommandTestUtil.PARCEL_DESC_LAZADA;
import static bookopedia.logic.commands.CommandTestUtil.PARCEL_DESC_SHOPEE;
import static bookopedia.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static bookopedia.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static bookopedia.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static bookopedia.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static bookopedia.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static bookopedia.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static bookopedia.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static bookopedia.logic.commands.CommandTestUtil.VALID_PARCEL_LAZADA;
import static bookopedia.logic.commands.CommandTestUtil.VALID_PARCEL_SHOPEE;
import static bookopedia.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static bookopedia.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static bookopedia.logic.parser.CliSyntax.PREFIX_PARCEL;
import static bookopedia.logic.parser.CommandParserTestUtil.assertParseFailure;
import static bookopedia.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static bookopedia.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static bookopedia.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static bookopedia.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import bookopedia.commons.core.index.Index;
import bookopedia.logic.commands.EditCommand;
import bookopedia.logic.commands.EditCommand.EditPersonDescriptor;
import bookopedia.model.parcel.Parcel;
import bookopedia.model.person.Address;
import bookopedia.model.person.Email;
import bookopedia.model.person.Name;
import bookopedia.model.person.Phone;
import bookopedia.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String PARCEL_EMPTY = " " + PREFIX_PARCEL;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS); // invalid address
        assertParseFailure(parser, "1" + INVALID_PARCEL_DESC, Parcel.MESSAGE_CONSTRAINTS); // invalid parcel

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // valid phone followed by invalid phone. The test case for invalid phone followed by valid phone
        // is tested at {@code parse_invalidValueFollowedByValidValue_success()}
        assertParseFailure(parser, "1" + PHONE_DESC_BOB + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the parcels of the {@code Person} being edited,
        // parsing it together with a valid parcel results in error
        assertParseFailure(parser, "1" + PARCEL_DESC_SHOPEE + PARCEL_DESC_LAZADA
                + PARCEL_EMPTY, Parcel.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + PARCEL_DESC_SHOPEE + PARCEL_EMPTY
                + PARCEL_DESC_LAZADA, Parcel.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + PARCEL_EMPTY + PARCEL_DESC_SHOPEE
                + PARCEL_DESC_LAZADA, Parcel.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC
                        + VALID_ADDRESS_AMY + VALID_PHONE_AMY, Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + PARCEL_DESC_LAZADA
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + NAME_DESC_AMY + PARCEL_DESC_SHOPEE;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withParcels(VALID_PARCEL_LAZADA, VALID_PARCEL_SHOPEE).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // address
        userInput = targetIndex.getOneBased() + ADDRESS_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // parcels
        userInput = targetIndex.getOneBased() + PARCEL_DESC_SHOPEE;
        descriptor = new EditPersonDescriptorBuilder().withParcels(VALID_PARCEL_SHOPEE).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_acceptsLast() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + PARCEL_DESC_SHOPEE + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + PARCEL_DESC_SHOPEE
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + PARCEL_DESC_LAZADA;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB).withAddress(VALID_ADDRESS_BOB)
                .withParcels(VALID_PARCEL_SHOPEE, VALID_PARCEL_LAZADA)
                .build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_invalidValueFollowedByValidValue_success() {
        // no other valid values specified
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // other valid values specified
        userInput = targetIndex.getOneBased() + EMAIL_DESC_BOB + INVALID_PHONE_DESC + ADDRESS_DESC_BOB
                + PHONE_DESC_BOB;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_resetParcels_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + PARCEL_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withParcels().build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }
}
