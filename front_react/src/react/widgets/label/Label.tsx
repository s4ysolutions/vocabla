interface Props extends React.LabelHTMLAttributes<HTMLLabelElement> {
    title?: string
}

const Label: React.FC<Props> = ({ title, className, children, ...props }) =>
    <label className={`block text-sm font-medium text-gray-700 ${className || ''}`} {...props}>
        {title}
        {children}
    </label>

export default Label
