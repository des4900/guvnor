package org.drools.guvnor.client.decisiontable.cells;

import java.util.Date;

import org.drools.guvnor.client.messages.Constants;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * A Popup Date Editor.
 * 
 * @author manstis
 * 
 */
public class PopupDateEditCell extends AbstractEditableCell<Date, Date> {

	private int offsetX = 5;
	private int offsetY = 5;
	private Object lastKey;
	private Element lastParent;
	private Date lastValue;
	private final PopupPanel panel;
	private final DatePicker datePicker;
	private final DateTimeFormat format;
	private final CheckBox checkBox;
	private final VerticalPanel vPanel;
	private final SafeHtmlRenderer<String> renderer;
	private ValueUpdater<Date> valueUpdater;
	private boolean emptyValue = false;

	private Constants constants = GWT.create(Constants.class);

	public PopupDateEditCell(DateTimeFormat format) {
		this(format, SimpleSafeHtmlRenderer.getInstance());
	}

	public PopupDateEditCell(DateTimeFormat format,
			SafeHtmlRenderer<String> renderer) {
		super("click", "keydown");
		if (renderer == null) {
			throw new IllegalArgumentException("renderer == null");
		}
		if (format == null) {
			throw new IllegalArgumentException("format == null");
		}

		this.format = format;
		this.renderer = renderer;
		this.datePicker = new DatePicker();
		this.checkBox = new CheckBox(constants.EmptyValue());
		this.vPanel = new VerticalPanel();

		// Pressing ESCAPE dismisses the pop-up loosing any changes
		this.panel = new PopupPanel(true, true) {
			@Override
			protected void onPreviewNativeEvent(NativePreviewEvent event) {
				if (Event.ONKEYUP == event.getTypeInt()) {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
						panel.hide();
					}
				}
			}

		};

		// Closing the pop-up commits the change
		panel.addCloseHandler(new CloseHandler<PopupPanel>() {
			public void onClose(CloseEvent<PopupPanel> event) {
				lastKey = null;
				lastValue = null;
				if (lastParent != null && !event.isAutoClosed()) {
					lastParent.focus();
				} else if (event.isAutoClosed()) {
					commit();
				}
				lastParent = null;
			}
		});

		// Tabbing forward out of the CheckBox commits changes
		checkBox.addKeyDownHandler(new KeyDownHandler() {

			public void onKeyDown(KeyDownEvent event) {
				boolean keyShift = event.isShiftKeyDown();
				boolean keyTab = event.getNativeKeyCode() == KeyCodes.KEY_TAB;
				if (keyTab) {
					if (!keyShift) {
						commit();
					} else if (!datePicker.isVisible()) {
						commit();
					}
				}
			}

		});

		// Enable TextBox if not an empty value
		checkBox.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				emptyValue = checkBox.getValue();
				datePicker.setVisible(!emptyValue);
			}

		});

		// Hide the panel and call valueUpdater.update when a date is selected
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			public void onValueChange(ValueChangeEvent<Date> event) {
				// Remember the values before hiding the popup.
				Element cellParent = lastParent;
				Date oldValue = lastValue;
				Object key = lastKey;
				panel.hide();

				// Update the cell and value updater.
				Date date = event.getValue();
				setViewData(key, date);
				setValue(cellParent, oldValue, key);
				if (valueUpdater != null) {
					valueUpdater.update(date);
				}
			}
		});

		vPanel.add(datePicker);
		vPanel.add(checkBox);
		vPanel.setCellHeight(this.checkBox, "32px");
		vPanel.setCellVerticalAlignment(this.checkBox,
				VerticalPanel.ALIGN_MIDDLE);
		panel.add(vPanel);

	}

	// Commit the change
	protected void commit() {
		Date date = datePicker.getValue();
		if (emptyValue) {
			date = null;
		}
		commit(date);
	}

	// Commit the change
	protected void commit(Date date) {
		// Hide pop-up
		Element cellParent = lastParent;
		Date oldValue = lastValue;
		Object key = lastKey;
		panel.hide();

		// Update values
		setViewData(key, date);
		setValue(cellParent, oldValue, key);
		if (valueUpdater != null) {
			valueUpdater.update(date);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.cell.client.AbstractEditableCell#isEditing(com.google.
	 * gwt.dom.client.Element, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean isEditing(Element parent, Date value, Object key) {
		return lastKey != null && lastKey.equals(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.cell.client.AbstractCell#onBrowserEvent(com.google.gwt
	 * .dom.client.Element, java.lang.Object, java.lang.Object,
	 * com.google.gwt.dom.client.NativeEvent,
	 * com.google.gwt.cell.client.ValueUpdater)
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void onBrowserEvent(final Element parent, Date value, Object key,
			NativeEvent event, ValueUpdater<Date> valueUpdater) {

		super.onBrowserEvent(parent, value, key, event, valueUpdater);

		if (event.getType().equals("click")) {

			this.lastKey = key;
			this.lastParent = parent;
			this.lastValue = value;
			this.valueUpdater = valueUpdater;

			Date viewData = getViewData(key);
			Date date = (viewData == null) ? value : viewData;

			emptyValue = (date == null);
			datePicker.setVisible(!emptyValue);
			checkBox.setValue(emptyValue);

			// Default date
			if (emptyValue) {
				Date d = new Date();
				int year = d.getYear();
				int month = d.getMonth();
				int dom = d.getDate();
				date = new Date(year, month, dom);
			}
			datePicker.setCurrentMonth(date);
			datePicker.setValue(date);

			panel.setPopupPositionAndShow(new PositionCallback() {
				public void setPosition(int offsetWidth, int offsetHeight) {
					panel.setPopupPosition(parent.getAbsoluteLeft() + offsetX,
							parent.getAbsoluteTop() + offsetY);

					// Focus the first enabled control
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {

						public void execute() {
							if (emptyValue) {
								// checkBox.setFocus(true);
							}
						}

					});
				}
			});

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.cell.client.AbstractCell#render(java.lang.Object,
	 * java.lang.Object, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
	 */
	@Override
	public void render(Date value, Object key, SafeHtmlBuilder sb) {
		// Get the view data.
		Date viewData = getViewData(key);
		if (viewData != null && viewData.equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		String s = null;
		if (viewData != null) {
			s = format.format(viewData);
		} else if (value != null) {
			s = format.format(value);
		}
		if (s != null) {
			sb.append(renderer.render(s));
		}
	}

}
