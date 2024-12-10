package org.antlr.intellij.adaptor.test;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import org.jetbrains.org.objectweb.asm.MethodVisitor;
import org.jetbrains.org.objectweb.asm.Opcodes;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

/**
 * A generic class for testing languages. Note that multiple instances of a language class cannot be used
 * and will be rejected by the IntelliJ runtime; instead, a subclass must be generated for each test case,
 * hence the <code>abstract</code> label.
 */
public abstract class TestLanguage extends Language{
	
	protected TestLanguage(@NonNls @NotNull String lang){
		super(lang);
	}
	
	/**
	 * Generates a new subclass of {@linkplain TestLanguage}, and returns the canonical instance.
	 */
	public static TestLanguage synthesizeTestLanguage(@NotNull String lang){
		// Create a subclass of TestLanguage...
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		writer.visit(
				Opcodes.V1_8,
				Opcodes.ACC_SUPER | Opcodes.ACC_PUBLIC,
				"org/antlr/intellij/adaptor/test/$Lang" + lang,
				"",
				"org/antlr/intellij/adaptor/test/TestLanguage",
				new String[0]
		);
		
		// ...with one constructor that takes 0 parameters and has no type variables...
		MethodVisitor ctor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", "", new String[0]);
		ctor.visitCode();
		// ...that invokes the super constructor with itself and the language name...
		ctor.visitVarInsn(Opcodes.ALOAD, 0);
		ctor.visitLdcInsn(lang);
		ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/antlr/intellij/adaptor/test/TestLanguage", "<init>", "(Ljava/lang/String;)V", false);
		ctor.visitInsn(Opcodes.RETURN);
		ctor.visitMaxs(0, 0);
		ctor.visitEnd();
		writer.visitEnd();
		
		try{
			// ...then define this class and return one instance.
			Class<?> cls = MethodHandles.lookup().defineClass(writer.toByteArray());
			return (TestLanguage)cls.getConstructor().newInstance();
		}catch(IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e){
			throw new RuntimeException(e);
		}
	}
}