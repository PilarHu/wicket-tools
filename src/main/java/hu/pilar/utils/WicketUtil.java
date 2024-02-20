/*
 * Copyright 2009-2024 Pilar Internet Consulting Kft.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package hu.pilar.utils;

import static java.lang.Boolean.TRUE;
import static org.apache.wicket.AttributeModifier.append;

import hu.pilar.utils.wicket.AuditLoggableSession;
import hu.pilar.utils.wicket.NonNullReturningPropertyVariableInterpolator;
import java.io.Serializable;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.danekja.java.util.function.serializable.SerializableConsumer;
import org.danekja.java.util.function.serializable.SerializableSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WicketUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(WicketUtil.class);

  private WicketUtil() {}

  public static WebMarkupContainer wmc(String id) {
    return new WebMarkupContainer(id);
  }

  public static WebMarkupContainer wmco(String id) {
    return (WebMarkupContainer) new WebMarkupContainer(id).setOutputMarkupId(true);
  }

  public static <T extends Page> BookmarkablePageLink<T> bmpl(
      String id, Class<T> page, PageParameters pp) {
    return new BookmarkablePageLink<>(id, page, pp);
  }

  public static <T extends Component> T behave(T component, Behavior... b) {
    component.add(b);
    return component;
  }

  public static Behavior attribute(String name, String value) {
    return AttributeModifier.append(name, value);
  }

  public static Behavior attribute(String name, IModel<String> value) {
    return AttributeModifier.append(name, value);
  }

  public static <T extends Component> T visible(final T component, final boolean visible) {
    component.setVisible(visible);
    return component;
  }

  public static void tryShowError(CheckedRunnable<?> runnable, Component errorReporting) {
    try {
      runnable.run();
    } catch (Exception ex) {
      if (Session.get() instanceof AuditLoggableSession als) {
        als.auditLog(ex);
      }
      LOGGER.error("", ex);
      errorReporting.error(ex.getMessage());
    }
  }

  public static <T extends Serializable> IModel<T> lambda(
      SerializableSupplier<T> getter, SerializableConsumer<T> setter) {
    return LambdaModel.of(getter, setter);
  }

  public static <T extends Serializable> IModel<T> lambda(SerializableSupplier<T> getter) {
    return LambdaModel.of(getter);
  }

  public static String interpolate(String value, Object object) {
    return new NonNullReturningPropertyVariableInterpolator(value, object).toString();
  }

  public static AttributeAppender css(final String cssClass) {
    return append("class", cssClass);
  }

  public static AttributeAppender css(final IModel<String> cssClass) {
    return append("class", cssClass);
  }

  public static AttributeAppender tooltip(final String tooltip) {
    return append("title", tooltip);
  }

  public static AttributeAppender tooltip(final IModel<String> tooltip) {
    return append("title", tooltip);
  }

  public static Component addCss(final Component item, final String cssClass) {
    if (cssClass != null) {
      item.add(append("class", cssClass));
    }
    return item;
  }

  public static Component addCss(final Component item, final IModel<String> cssClass) {
    if (cssClass != null) {
      item.add(append("class", cssClass));
    }
    return item;
  }

  public static void runFeedbackAnyException(CheckedRunnable<?> runnable, Component component) {
    try {
      runnable.run();
    } catch (Exception ex) {
      component.error(ex.getMessage());
      LOGGER.debug(ex.getMessage(), ex);
    }
  }

  public static boolean hasThisComponent(AjaxRequestTarget target, Component component) {
    for (Component comp : target.getComponents()) {
      if (comp.equals(component)) {
        return true;
      }
      Boolean ret =
          component.visitParents(
              MarkupContainer.class,
              (object, visit) -> {
                if (object.equals(comp)) {
                  visit.stop(TRUE);
                }
              });
      if (TRUE.equals(ret)) {
        return true;
      }
    }
    return false;
  }

  public static PageParameters pageParameters(Object... parameters) {
    if (parameters.length % 2 != 0) {
      throw new IllegalArgumentException("illegal number of arguments " + parameters.length);
    }
    final var pp = new PageParameters();
    for (int i = 0; i < parameters.length / 2; i++) {
      pp.add(parameters[2 * i].toString(), parameters[2 * i + 1]);
    }
    return pp;
  }

  public static IModel<String> localizedText(final String key) {
    return new ResourceModel(key);
  }
}
