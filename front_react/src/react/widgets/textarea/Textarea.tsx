type Props = React.TextareaHTMLAttributes<HTMLTextAreaElement>

const Textarea: React.FC<Props> = ({ className, ...props }) =>
    <textarea
        className={`flex-1 border border-gray-300 rounded px-2 py-1 resize-none ${className || ''}`}
        {...props}
    />

export default Textarea